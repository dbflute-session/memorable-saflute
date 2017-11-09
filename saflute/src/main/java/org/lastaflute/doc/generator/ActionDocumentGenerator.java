/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.lastaflute.doc.generator;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.struts.upload.FormFile;
import org.dbflute.jdbc.Classification;
import org.dbflute.optional.OptionalThing;
import org.dbflute.saflute.core.util.ContainerUtil;
import org.dbflute.saflute.web.action.ActionResolver;
import org.dbflute.saflute.web.action.UrlChain;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfReflectionUtil;
import org.dbflute.util.DfReflectionUtil.ReflectionFailureException;
import org.dbflute.util.DfStringUtil;
import org.lastaflute.core.json.JsonMappingOption;
import org.lastaflute.core.json.JsonMappingOption.JsonFieldNaming;
import org.lastaflute.doc.meta.ActionDocMeta;
import org.lastaflute.doc.meta.TypeDocMeta;
import org.lastaflute.doc.reflector.SourceParserReflector;
import org.lastaflute.doc.util.LaDocReflectionUtil;
import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.beans.factory.BeanDescFactory;
import org.seasar.framework.container.ComponentDef;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.struts.annotation.ActionForm;
import org.seasar.struts.annotation.Execute;

import com.google.gson.FieldNamingPolicy;

// package of this class should be under lastaflute but no fix for compatible
/**
 * @author p1us2er0
 * @author jflute
 * @since 0.5.0-sp9 of UTFlute (2015/09/18 Friday)
 */
public class ActionDocumentGenerator extends BaseDocumentGenerator {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** list of suppressed fields, e.g. enhanced fields by JaCoCo. */
    protected static final Set<String> SUPPRESSED_FIELD_SET;
    static {
        SUPPRESSED_FIELD_SET = DfCollectionUtil.newHashSet("$jacocoData");
    }

    protected static final List<String> TARGET_SUFFIX_LIST;
    static {
        TARGET_SUFFIX_LIST = Arrays.asList("Form", "Body", "Bean", "Result");
    }

    protected static final List<Class<?>> NATIVE_TYPE_LIST =
            Arrays.asList(void.class, boolean.class, byte.class, int.class, long.class, float.class, double.class, Void.class, Byte.class,
                    Boolean.class, Integer.class, Byte.class, Long.class, Float.class, Double.class, String.class, Map.class, byte[].class,
                    Byte[].class, Date.class, LocalDate.class, LocalDateTime.class, LocalTime.class, FormFile.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The list of source directory. (NotNull) */
    protected final List<String> srcDirList;

    /** depth of analyzed target, to avoid cyclic analyzing. */
    protected int depth; // #question depth count down? by jflute

    /** The optional reflector of source parser, e.g. java parser. (NotNull, EmptyAllowed) */
    protected final OptionalThing<SourceParserReflector> sourceParserReflector;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ActionDocumentGenerator(List<String> srcDirList, int depth, OptionalThing<SourceParserReflector> sourceParserReflector) {
        this.srcDirList = srcDirList;
        this.depth = depth;
        this.sourceParserReflector = sourceParserReflector;
    }

    // ===================================================================================
    //                                                                            Generate
    //                                                                            ========
    public List<ActionDocMeta> generateActionDocMetaList() { // the list is per execute method
        final List<String> actionComponentNameList = findActionComponentNameList();
        final List<ActionDocMeta> metaList = DfCollectionUtil.newArrayList();
        actionComponentNameList.forEach(componentName -> { // per action class
            Class<?> actionClass = getRootContainer().getComponentDef(componentName).getComponentClass();
            final List<Method> methodList = DfCollectionUtil.newArrayList();
            sourceParserReflector.ifPresent(sourceParserReflector -> {
                methodList.addAll(sourceParserReflector.getMethodListOrderByDefinition(actionClass));
            });
            if (methodList.isEmpty()) { // no java parser, use normal reflection
                methodList.addAll(Arrays.stream(actionClass.getMethods()).sorted(Comparator.comparing(method -> {
                    return method.getName();
                })).collect(Collectors.toList()));
            }
            methodList.forEach(method -> { // contains all methods
                if (method.getAnnotation(Execute.class) != null) { // only execute method here
                    if (!exceptsActionExecute(method)) {
                        final ActionDocMeta actionDocMeta = createActionDocMeta(method);
                        metaList.add(actionDocMeta);
                    }
                }
            });
        });
        return metaList;
    }

    protected boolean exceptsActionExecute(Method executeMethod) { // may be overridden
        if (suppressActionExecute(executeMethod)) { // for compatible
            return true;
        }
        return false;
    }

    @Deprecated
    protected boolean suppressActionExecute(Method executeMethod) {
        return false;
    }

    // ===================================================================================
    //                                                                      Action DocMeta
    //                                                                      ==============
    protected ActionDocMeta createActionDocMeta(Method executeMethod) {
        final ActionDocMeta actionDocMeta = new ActionDocMeta();
        final Class<?> actionClass = executeMethod.getDeclaringClass();
        final UrlChain urlChain = new UrlChain(actionClass);
        String urlPattern = executeMethod.getAnnotation(Execute.class).urlPattern();
        if (DfStringUtil.is_Null_or_Empty(urlPattern)) {
            urlPattern = executeMethod.getName();
        }
        if (!"index".equals(urlPattern)) {
            urlChain.moreUrl(urlPattern);
        }

        // action item
        // TODO p1us2er0 delete as slash duplicates. (2015/10/09)
        actionDocMeta.setUrl(getActionResolver().toActionUrl(actionClass, false, urlChain).replaceAll("//", "/"));

        // class item
        final Class<?> methodDeclaringClass = executeMethod.getDeclaringClass(); // basically same as componentClass
        actionDocMeta.setType(methodDeclaringClass);
        actionDocMeta.setTypeName(adjustTypeName(methodDeclaringClass));
        actionDocMeta.setSimpleTypeName(adjustSimpleTypeName(methodDeclaringClass));

        // field item
        actionDocMeta.setFieldTypeDocMetaList(Arrays.stream(methodDeclaringClass.getDeclaredFields()).map(field -> {
            final TypeDocMeta typeDocMeta = new TypeDocMeta();
            typeDocMeta.setName(field.getName());
            typeDocMeta.setType(field.getType());
            typeDocMeta.setTypeName(adjustTypeName(field.getGenericType()));
            typeDocMeta.setSimpleTypeName(adjustSimpleTypeName((field.getGenericType())));
            typeDocMeta.setAnnotationTypeList(Arrays.asList(field.getAnnotations()));
            typeDocMeta.setAnnotationList(analyzeAnnotationList(typeDocMeta.getAnnotationTypeList()));

            sourceParserReflector.ifPresent(sourceParserReflector -> {
                sourceParserReflector.reflect(typeDocMeta, field.getType());
            });
            return typeDocMeta;
        }).collect(Collectors.toList()));

        // method item
        actionDocMeta.setMethodName(executeMethod.getName());

        // annotation item
        final List<Annotation> annotationList = DfCollectionUtil.newArrayList();
        annotationList.addAll(Arrays.asList(methodDeclaringClass.getAnnotations()));
        annotationList.addAll(Arrays.asList(executeMethod.getAnnotations()));
        actionDocMeta.setAnnotationTypeList(annotationList); // contains both action and execute method
        actionDocMeta.setAnnotationList(analyzeAnnotationList(annotationList));

        // in/out item (parameter, form, return)
        actionDocMeta.setParameterTypeDocMetaList(Collections.emptyList());
        analyzeFormClass(actionClass).ifPresent(formTypeDocMeta -> {
            actionDocMeta.setFormTypeDocMeta(formTypeDocMeta);
        });
        actionDocMeta.setReturnTypeDocMeta(analyzeReturnClass(executeMethod));

        // extension item (url, return, comment...)
        sourceParserReflector.ifPresent(sourceParserReflector -> {
            sourceParserReflector.reflect(actionDocMeta, executeMethod);
        });

        return actionDocMeta;
    }

    // ===================================================================================
    //                                                                             Analyze
    //                                                                             =======
    // -----------------------------------------------------
    //                                          Analyze Form
    //                                          ------------
    protected OptionalThing<TypeDocMeta> analyzeFormClass(Class<?> actionClass) {
        BeanDesc beanDesc = BeanDescFactory.getBeanDesc(actionClass);
        Optional<TypeDocMeta> typeDocMeta = IntStream.range(0, beanDesc.getFieldSize()).mapToObj(index -> beanDesc.getField(index))
                .filter(field -> field.getAnnotation(ActionForm.class) != null).findFirst().map(field -> {
                    TypeDocMeta formDocMeta = new TypeDocMeta();
                    Class<?> formType = field.getType();
                    formDocMeta.setType(formType);
                    formDocMeta.setTypeName(adjustTypeName(formType));
                    formDocMeta.setSimpleTypeName(adjustSimpleTypeName(formType));
                    final Map<String, Type> genericParameterTypesMap = DfCollectionUtil.newLinkedHashMap(); // #question can be emptyList()? by jflute
                    final List<TypeDocMeta> propertyDocMetaList = analyzeProperties(formType, genericParameterTypesMap, depth);
                    formDocMeta.setNestTypeDocMetaList(propertyDocMetaList);
                    sourceParserReflector.ifPresent(sourceParserReflector -> {
                        sourceParserReflector.reflect(formDocMeta, formDocMeta.getType());
                    });
                    return formDocMeta;
                });
        return OptionalThing.migratedFrom(typeDocMeta, () -> {
            throw new IllegalStateException("Not found Form.");
        });
    }

    // -----------------------------------------------------
    //                                        Analyze Return
    //                                        --------------
    protected TypeDocMeta analyzeReturnClass(Method method) {
        final TypeDocMeta returnDocMeta = new TypeDocMeta();
        returnDocMeta.setType(method.getReturnType());
        returnDocMeta.setTypeName(adjustTypeName(method.getGenericReturnType()));
        returnDocMeta.setSimpleTypeName(adjustSimpleTypeName(method.getGenericReturnType()));
        returnDocMeta.setGenericType(DfReflectionUtil.getGenericFirstClass(method.getGenericReturnType()));
        returnDocMeta.setAnnotationTypeList(Arrays.asList(method.getAnnotatedReturnType().getAnnotations()));
        returnDocMeta.setAnnotationList(analyzeAnnotationList(returnDocMeta.getAnnotationTypeList()));
        derivedManualReturnClass(method, returnDocMeta);

        Class<?> returnClass = returnDocMeta.getGenericType();
        if (returnClass != null) { // e.g. List<String>, Sea<Land>
            // TODO p1us2er0 optimisation, generic handling in analyzeReturnClass() (2015/09/30)
            final Map<String, Type> genericParameterTypesMap = DfCollectionUtil.newLinkedHashMap();
            final Type[] parameterTypes = DfReflectionUtil.getGenericParameterTypes(method.getGenericReturnType());
            final TypeVariable<?>[] typeVariables = returnClass.getTypeParameters();
            IntStream.range(0, parameterTypes.length).forEach(parameterTypesIndex -> {
                final Type[] genericParameterTypes = DfReflectionUtil.getGenericParameterTypes(parameterTypes[parameterTypesIndex]);
                IntStream.range(0, typeVariables.length).forEach(typeVariablesIndex -> {
                    Type type = genericParameterTypes[typeVariablesIndex];
                    genericParameterTypesMap.put(typeVariables[typeVariablesIndex].getTypeName(), type);
                });
            });

            if (Iterable.class.isAssignableFrom(returnClass)) { // e.g. List<String>, List<Sea<Land>>
                returnClass = LaDocReflectionUtil.extractElementType(method.getGenericReturnType(), 1);
            }
            final List<Class<? extends Object>> nativeClassList = getNativeClassList();
            if (returnClass != null && !nativeClassList.contains(returnClass)) {
                final List<TypeDocMeta> propertyDocMetaList = analyzeProperties(returnClass, genericParameterTypesMap, depth);
                returnDocMeta.setNestTypeDocMetaList(propertyDocMetaList);
            }

            if (sourceParserReflector.isPresent()) {
                sourceParserReflector.get().reflect(returnDocMeta, returnClass);
            }
        }

        return returnDocMeta;
    }

    protected void derivedManualReturnClass(Method method, TypeDocMeta returnDocMeta) {
    }

    protected List<Class<?>> getNativeClassList() {
        return NATIVE_TYPE_LIST;
    }

    // -----------------------------------------------------
    //                                    Analyze Properties
    //                                    ------------------
    // for e.g. form type, return type, nested property type
    protected List<TypeDocMeta> analyzeProperties(Class<?> propertyOwner, Map<String, Type> genericParameterTypesMap, int depth) {
        if (depth < 0) {
            return DfCollectionUtil.newArrayList();
        }
        final Set<Field> fieldSet = extractWholeFieldSet(propertyOwner);
        return fieldSet.stream().filter(field -> { // also contains private fields and super's fields
            return !exceptsField(field);
        }).map(field -> { // #question contains private fields, right? by jflute
            return analyzePropertyField(propertyOwner, genericParameterTypesMap, depth, field);
        }).collect(Collectors.toList());
    }

    protected Set<Field> extractWholeFieldSet(Class<?> propertyOwner) {
        final Set<Field> fieldSet = DfCollectionUtil.newLinkedHashSet();
        for (Class<?> targetClazz = propertyOwner; targetClazz != Object.class; targetClazz = targetClazz.getSuperclass()) {
            if (targetClazz == null) { // e.g. interface: MultipartFormFile
                break;
            }
            fieldSet.addAll(Arrays.asList(targetClazz.getDeclaredFields()));
        }
        return fieldSet;
    }

    protected boolean exceptsField(Field field) { // e.g. special field and static field
        return SUPPRESSED_FIELD_SET.contains(field.getName()) || Modifier.isStatic(field.getModifiers());
    }

    // -----------------------------------------------------
    //                                Analyze Property Field
    //                                ----------------------
    protected TypeDocMeta analyzePropertyField(Class<?> propertyOwner, Map<String, Type> genericParameterTypesMap, int depth, Field field) {
        final TypeDocMeta meta = new TypeDocMeta();

        final Class<?> resolvedClass;
        {
            final Type resolvedType;
            {
                final Type genericClass = genericParameterTypesMap.get(field.getGenericType().getTypeName());
                resolvedType = genericClass != null ? genericClass : field.getType();
            }

            // basic item
            meta.setName(field.getName()); // also property name #question but overridden later, needed? by jflute
            meta.setType(field.getType()); // e.g. String, Integer, SeaPart #question not use resolvedType, right? by jflute
            meta.setTypeName(adjustTypeName(resolvedType));
            meta.setSimpleTypeName(adjustSimpleTypeName(resolvedType));

            // annotation item
            meta.setAnnotationTypeList(Arrays.asList(field.getAnnotations()));
            meta.setAnnotationList(analyzeAnnotationList(meta.getAnnotationTypeList()));

            // comment item (value expression)
            if (resolvedType instanceof Class) {
                resolvedClass = (Class<?>) resolvedType;
            } else {
                resolvedClass = (Class<?>) DfReflectionUtil.getGenericParameterTypes(resolvedType)[0];
            }
            if (resolvedClass.isEnum()) {
                meta.setValue(buildEnumValuesExp(resolvedClass)); // e.g. {FML = Formalized, PRV = Provisinal, ...}
            }
        }

        if (isTargetSuffixResolvedClass(resolvedClass)) { // nested bean of direct type as top or inner class
            // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
            // e.g.
            //  public SeaResult sea; // current field, the outer type should have suffix
            //
            //   or
            //
            //  public class SeaResult { // the declaring class should have suffix
            //      public HangarPart hangar; // current field
            //      public static class HangarPart {
            //          ...
            //      }
            //  }
            // _/_/_/_/_/_/_/_/_/_/
            meta.setNestTypeDocMetaList(analyzeProperties(resolvedClass, genericParameterTypesMap, depth - 1));
        } else if (isTargetSuffixFieldGeneric(field)) { // nested bean of generic type as top or inner class
            // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
            // e.g.
            //  public List<SeaResult> seaList; // current field, the outer type should have suffix
            //
            //   or
            //
            //  public class SeaResult { // the declaring class should have suffix
            //      public List<HangarPart> hangarList; // current field
            //      public static class HangarPart {
            //          ...
            //      }
            //  }
            // _/_/_/_/_/_/_/_/_/_/
            Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            if (type instanceof Class<?>) {
                final Class<?> typeArgumentClass = (Class<?>) type;
                meta.setNestTypeDocMetaList(analyzeProperties(typeArgumentClass, genericParameterTypesMap, depth - 1));
                // overriding type names that are already set before
                final String currentTypeName = meta.getTypeName();
                meta.setTypeName(adjustTypeName(currentTypeName) + "<" + adjustTypeName(typeArgumentClass) + ">");
                meta.setSimpleTypeName(adjustSimpleTypeName(currentTypeName) + "<" + adjustSimpleTypeName(typeArgumentClass) + ">");
            } else if (type instanceof ParameterizedType) {
                final Class<?> typeArgumentClass = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
                meta.setNestTypeDocMetaList(analyzeProperties(typeArgumentClass, genericParameterTypesMap, depth - 1));
                // overriding type names that are already set before
                final String currentTypeName = meta.getTypeName();
                meta.setTypeName(adjustTypeName(currentTypeName) + "<" + adjustTypeName(((ParameterizedType) type).getRawType()) + "<"
                        + adjustTypeName(typeArgumentClass) + ">>");
                meta.setSimpleTypeName(
                        adjustSimpleTypeName(currentTypeName) + "<" + adjustSimpleTypeName(((ParameterizedType) type).getRawType()) + "<"
                                + adjustSimpleTypeName(typeArgumentClass) + ">>");
            }
        } else { // e.g. String, Integer, LocalDate, Sea<Mystic>
            // TODO p1us2er0 optimisation, generic handling in analyzePropertyField() (2017/09/26)
            if (field.getGenericType().getTypeName().matches(".*<(.*)>")) { // e.g. Sea<Mystic>
                final String genericTypeName = field.getGenericType().getTypeName().replaceAll(".*<(.*)>", "$1");

                // generic item
                try {
                    meta.setGenericType(DfReflectionUtil.forName(genericTypeName));
                } catch (ReflectionFailureException ignored) { // e.g. BEAN (generic parameter name)
                    meta.setGenericType(Object.class); // unknown
                }

                final Type genericClass = genericParameterTypesMap.get(genericTypeName);
                if (genericClass != null) { // the generic is defined at top definition (e.g. return)
                    meta.setNestTypeDocMetaList(analyzeProperties((Class<?>) genericClass, genericParameterTypesMap, depth - 1));

                    // overriding type names that are already set before
                    final String typeName = meta.getTypeName();
                    meta.setTypeName(adjustTypeName(typeName) + "<" + adjustTypeName(genericClass) + ">");
                    meta.setSimpleTypeName(adjustSimpleTypeName(typeName) + "<" + adjustSimpleTypeName(genericClass) + ">");
                } else {
                    // overriding type names that are already set before
                    final String typeName = meta.getTypeName();
                    meta.setTypeName(adjustTypeName(typeName) + "<" + adjustSimpleTypeName(genericTypeName) + ">"); // #question why simple? by jflute
                    meta.setSimpleTypeName(adjustSimpleTypeName(typeName) + "<" + adjustSimpleTypeName(genericTypeName) + ">");
                }
            }
        }

        // e.g. comment item (description, comment)
        sourceParserReflector.ifPresent(sourceParserReflector -> {
            sourceParserReflector.reflect(meta, propertyOwner);
        });

        // necessary to set it after parsing javadoc
        meta.setName(adjustFieldName(propertyOwner, field));
        meta.setPublicName(adjustPublicFieldName(propertyOwner, field));
        return meta;
    }

    protected String buildEnumValuesExp(Class<?> typeClass) {
        // cannot resolve type by maven compiler, explicitly cast it
        final String valuesExp;
        if (Classification.class.isAssignableFrom(typeClass)) {
            @SuppressWarnings("unchecked")
            final Class<Classification> clsType = ((Class<Classification>) typeClass);
            valuesExp = Arrays.stream(clsType.getEnumConstants()).collect(Collectors.toMap(keyMapper -> {
                return ((Classification) keyMapper).code();
            }, valueMapper -> {
                return ((Classification) valueMapper).alias();
            }, (u, v) -> v, LinkedHashMap::new)).toString(); // e.g. {FML = Formalized, PRV = Provisinal, ...}
        } else {
            final Enum<?>[] constants = (Enum<?>[]) typeClass.getEnumConstants();
            valuesExp = Arrays.stream(constants).collect(Collectors.toList()).toString(); // e.g. [SEA, LAND, PIARI]
        }
        return valuesExp;
    }

    protected boolean isTargetSuffixResolvedClass(Class<?> resolvedClass) {
        // #question suffix but contains? for AllSuffixResult$ResortParkPart? by jflute
        return getTargetTypeSuffixList().stream().anyMatch(suffix -> resolvedClass.getName().contains(suffix));
    }

    protected boolean isTargetSuffixFieldGeneric(Field field) {
        // #question suffix but contains? by jflute
        return getTargetTypeSuffixList().stream().anyMatch(suffix -> field.getGenericType().getTypeName().contains(suffix));
    }

    protected List<String> getTargetTypeSuffixList() {
        return TARGET_SUFFIX_LIST;
    }

    protected String adjustFieldName(Class<?> clazz, Field field) {
        return field.getName();
    }

    protected String adjustPublicFieldName(Class<?> clazz, Field field) {
        // TODO p1us2er0 judge accurately in adjustFieldName() (2017/04/20)
        if (clazz.getSimpleName().endsWith("Form")) {
            return field.getName();
        }
        return getApplicationJsonMappingOption().flatMap(jsonMappingOption -> {
            return jsonMappingOption.getFieldNaming().map(naming -> {
                if (naming == JsonFieldNaming.IDENTITY) {
                    return FieldNamingPolicy.IDENTITY.translateName(field);
                } else if (naming == JsonFieldNaming.CAMEL_TO_LOWER_SNAKE) {
                    return FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field);
                } else {
                    return field.getName();
                }
            });
        }).orElse(field.getName());
    }

    // ===================================================================================
    //                                                                     Action Property
    //                                                                     ===============
    // #question who calls? by jflute
    public Map<String, Map<String, String>> generateActionPropertyNameMap(List<ActionDocMeta> actionDocMetaList) {
        final Map<String, Map<String, String>> propertyNameMap = actionDocMetaList.stream().collect(Collectors.toMap(key -> {
            return key.getUrl().replaceAll("\\{.*", "").replaceAll("/$", "").replaceAll("/", "_");
        }, value -> {
            return convertPropertyNameMap("", value.getFormTypeDocMeta());
        }, (u, v) -> v, LinkedHashMap::new));
        return propertyNameMap;
    }

    protected Map<String, String> convertPropertyNameMap(String parentName, TypeDocMeta typeDocMeta) {
        if (typeDocMeta == null) {
            return DfCollectionUtil.newLinkedHashMap();
        }

        final Map<String, String> propertyNameMap = DfCollectionUtil.newLinkedHashMap();

        final String name = calculateName(parentName, typeDocMeta.getName(), typeDocMeta.getTypeName());
        if (DfStringUtil.is_NotNull_and_NotEmpty(name)) {
            propertyNameMap.put(name, "");
        }

        if (typeDocMeta.getNestTypeDocMetaList() != null) {
            typeDocMeta.getNestTypeDocMetaList().forEach(nestDocMeta -> {
                propertyNameMap.putAll(convertPropertyNameMap(name, nestDocMeta));
            });
        }

        return propertyNameMap;
    }

    protected String calculateName(String parentName, String name, String type) {
        if (DfStringUtil.is_Null_or_Empty(name)) {
            return null;
        }

        final StringBuilder builder = new StringBuilder();
        if (DfStringUtil.is_NotNull_and_NotEmpty(parentName)) {
            builder.append(parentName + ".");
        }
        builder.append(name);
        if (name.endsWith("List")) {
            builder.append("[]");
        }

        return builder.toString();
    }

    // ===================================================================================
    //                                                                        DI Container
    //                                                                        ============
    protected List<String> findActionComponentNameList() {
        final List<String> componentNameList = DfCollectionUtil.newArrayList();
        final S2Container container = getRootContainer();
        srcDirList.stream().filter(srcDir -> Paths.get(srcDir).toFile().exists()).forEach(srcDir -> {
            try (Stream<Path> stream = Files.find(Paths.get(srcDir), Integer.MAX_VALUE, (path, attr) -> {
                return path.toString().endsWith("Action.java");
            })) {
                stream.sorted().map(path -> {
                    final String className = extractActionClassName(path, srcDir);
                    return DfReflectionUtil.forName(className);
                }).filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())).forEach(clazz -> {
                    final String componentName = container.getComponentDef(clazz).getComponentName();
                    if (componentName != null && !componentNameList.contains(componentName)) {
                        componentNameList.add(componentName);
                    }
                });
            } catch (IOException e) {
                throw new IllegalStateException("Failed to find the components: " + srcDir, e);
            }
        });
        IntStream.range(0, container.getComponentDefSize()).forEach(index -> {
            final ComponentDef componentDef = container.getComponentDef(index);
            final String componentName = componentDef.getComponentName();
            if (componentName.endsWith("Action") && !componentNameList.contains(componentName)) {
                componentNameList.add(componentDef.getComponentName());
            }
        });
        return componentNameList;
    }

    protected String extractActionClassName(Path path, String srcDir) { // for forName()
        String className = DfStringUtil.substringFirstRear(path.toFile().getAbsolutePath(), new File(srcDir).getAbsolutePath());
        if (className.startsWith(File.separator)) {
            className = className.substring(1);
        }
        className = DfStringUtil.substringLastFront(className, ".java").replace(File.separatorChar, '.');
        return className;
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected DocumentGeneratorFactory createDocumentGeneratorFactory() {
        return new DocumentGeneratorFactory();
    }

    protected S2Container getRootContainer() {
        return SingletonS2ContainerFactory.getContainer().getRoot();
    }

    protected ActionResolver getActionResolver() {
        return ContainerUtil.getComponent(ActionResolver.class);
    }

    protected OptionalThing<JsonMappingOption> getApplicationJsonMappingOption() {
        return createDocumentGeneratorFactory().getApplicationJsonMappingOption();
    }
}
