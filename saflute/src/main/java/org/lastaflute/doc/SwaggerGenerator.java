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
package org.lastaflute.doc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.upload.FormFile;
import org.dbflute.jdbc.Classification;
import org.dbflute.optional.OptionalThing;
import org.dbflute.saflute.core.direction.AccessibleConfig;
import org.dbflute.saflute.core.util.ContainerUtil;
import org.dbflute.saflute.web.action.api.JsonParameter;
import org.dbflute.saflute.web.action.response.ActionResponse;
import org.dbflute.saflute.web.action.response.JsonResponse;
import org.dbflute.saflute.web.action.response.StreamResponse;
import org.dbflute.saflute.web.action.response.XmlResponse;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfReflectionUtil;
import org.dbflute.util.DfResourceUtil;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.DfTypeUtil;
import org.lastaflute.core.json.JsonMappingOption;
import org.lastaflute.core.json.annotation.JsonDatePattern;
import org.lastaflute.core.json.engine.RealJsonEngine;
import org.lastaflute.di.helper.misc.ParameterizedRef;
import org.lastaflute.doc.generator.ActionDocumentGenerator;
import org.lastaflute.doc.generator.DocumentGeneratorFactory;
import org.lastaflute.doc.meta.ActionDocMeta;
import org.lastaflute.doc.meta.TypeDocMeta;
import org.lastaflute.doc.web.LaActionSwaggerable;
import org.seasar.framework.util.tiger.Tuple3;
import org.seasar.struts.annotation.Maxbytelength;
import org.seasar.struts.annotation.Maxlength;
import org.seasar.struts.annotation.Minbytelength;
import org.seasar.struts.annotation.Minlength;
import org.seasar.struts.annotation.Required;
import org.seasar.struts.util.RequestUtil;

/**
 * @author p1us2er0
 * @author jflute
 */
public class SwaggerGenerator {

    // e.g. SwaggerAction implementation
    //@AllowAnyoneAccess
    //public class SwaggerAction extends FortressBaseAction {
    //
    //    // ===================================================================================
    //    //                                                                           Attribute
    //    //                                                                           =========
    //    @Resource
    //    private RequestManager requestManager;
    //    @Resource
    //    private FortressConfig config;
    //
    //    // ===================================================================================
    //    //                                                                             Execute
    //    //                                                                             =======
    //    @Execute
    //    public HtmlResponse index() {
    //        verifySwaggerAllowed();
    //        String swaggerJsonUrl = toActionUrl(SwaggerAction.class, moreUrl("json"));
    //        return new SwaggerAgent(requestManager).prepareSwaggerUiResponse(swaggerJsonUrl);
    //    }
    //
    //    @Execute
    //    public JsonResponse<Map<String, Object>> json() {
    //        verifySwaggerAllowed();
    //        return asJson(new SwaggerGenerator().generateSwaggerMap());
    //    }
    //
    //    private void verifySwaggerAllowed() { // also check in ActionAdjustmentProvider
    //        verifyOrClientError("Swagger is not enabled.", config.isSwaggerEnabled());
    //    }
    //}

    // e.g. LastaDocTest implementation
    //public class ShowbaseLastaDocTest extends UnitShowbaseTestCase {
    //
    //    @Override
    //    protected String prepareMockContextPath() {
    //        return ShowbaseBoot.CONTEXT; // basically for swagger
    //    }
    //
    //    public void test_document() throws Exception {
    //        saveLastaDocMeta();
    //    }
    //
    //    public void test_swaggerJson() throws Exception {
    //        saveSwaggerMeta(new SwaggerAction());
    //    }
    //}

    // ===================================================================================
    //                                                                            Generate
    //                                                                            ========
    // basically called by action
    /**
     * Generate swagger map. (no option)
     * @return The map of swagger information. (NotNull)
     */
    public Map<String, Object> generateSwaggerMap() {
        return generateSwaggerMap(op -> {});
    }

    /**
     * Generate swagger map with option.
     * <pre>
     * new SwaggerGenerator().generateSwaggerMap(op -&gt; {
     *     op.deriveBasePath(basePath -&gt; basePath + "api/");
     * });
     * </pre>
     * @param opLambda The callback for settings of option. (NotNull)
     * @return The map of swagger information. (NotNull)
     */
    public Map<String, Object> generateSwaggerMap(Consumer<SwaggerOption> opLambda) {
        final OptionalThing<Map<String, Object>> swaggerJson = readSwaggerJson();
        if (swaggerJson.isPresent()) { // e.g. war world
            final Map<String, Object> swaggerMap = swaggerJson.get();
            swaggerMap.put("schemes", prepareSwaggerMapSchemes());
            return swaggerMap;
        }
        // basically here in local development
        final SwaggerOption swaggerOption = createSwaggerOption(opLambda);
        return createSwaggerMap(swaggerOption);
    }

    protected SwaggerOption createSwaggerOption(Consumer<SwaggerOption> opLambda) {
        final SwaggerOption swaggerOption = new SwaggerOption();
        opLambda.accept(swaggerOption);
        return swaggerOption;
    }

    // ===================================================================================
    //                                                                               Save
    //                                                                              ======
    // basically called by unit test
    public void saveSwaggerMeta(LaActionSwaggerable swaggerable) {
        final String json = createJsonEngine().toJson(swaggerable.json().getJsonObj());

        final Path path = Paths.get(getLastaDocDir(), "swagger.json");
        final Path parentPath = path.getParent();
        if (!Files.exists(parentPath)) {
            try {
                Files.createDirectories(parentPath);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create directory: " + parentPath, e);
            }
        }

        try (BufferedWriter bw = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
            bw.write(json);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write the json to the file: " + path, e);
        }
    }

    // ===================================================================================
    //                                                                        swagger.json
    //                                                                        ============
    protected OptionalThing<Map<String, Object>> readSwaggerJson() { // for war world
        String swaggerJsonFilePath = "./swagger.json";
        if (!DfResourceUtil.isExist(swaggerJsonFilePath)) {
            return OptionalThing.empty();
        }

        try (InputStream inputStream = DfResourceUtil.getResourceStream(swaggerJsonFilePath);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String json = DfResourceUtil.readText(bufferedReader);
            return OptionalThing.of(createJsonEngine().fromJsonParameteried(json, new ParameterizedRef<Map<String, Object>>() {
            }.getType()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read the json to the file: " + swaggerJsonFilePath, e);
        }
    }

    // ===================================================================================
    //                                                                         Swagger Map
    //                                                                         ===========
    protected Map<String, Object> createSwaggerMap(SwaggerOption swaggerOption) {
        // process order is order in swagger.json is here
        final Map<String, Object> swaggerMap = DfCollectionUtil.newLinkedHashMap();
        swaggerMap.put("swagger", "2.0");
        swaggerMap.put("info", createSwaggerInfoMap());
        swaggerMap.put("schemes", prepareSwaggerMapSchemes());
        swaggerMap.put("basePath", derivedBasePath(swaggerOption));

        final List<Map<String, Object>> swaggerTagList = DfCollectionUtil.newArrayList();
        swaggerMap.put("tags", swaggerTagList);

        // security has no constraint of order but should be before paths for swagger.json view
        swaggerOption.getSecurityDefinitionList().ifPresent(securityDefinitionList -> {
            adaptSecurityDefinitions(swaggerMap, securityDefinitionList);
        });

        final Map<String, Map<String, Object>> swaggerPathMap = DfCollectionUtil.newLinkedHashMap();
        swaggerMap.put("paths", swaggerPathMap);

        final Map<String, Map<String, Object>> swaggerDefinitionsMap = DfCollectionUtil.newLinkedHashMap();
        swaggerMap.put("definitions", swaggerDefinitionsMap);

        setupSwaggerPathMap(swaggerPathMap, swaggerDefinitionsMap, swaggerTagList);

        // header is under paths so MUST be after paths setup
        swaggerOption.getHeaderParameterList().ifPresent(headerParameterList -> {
            adaptHeaderParameters(swaggerMap, headerParameterList); // needs paths in swaggerMap
        });
        return swaggerMap;
    }

    protected Map<String, String> createSwaggerInfoMap() {
        Map<String, String> swaggerInfoMap = DfCollectionUtil.newLinkedHashMap();
        String title = Objects.toString(getAccessibleConfig().get("domain.title"), getAccessibleConfig().get("domain.name"));
        swaggerInfoMap.put("title", title);
        swaggerInfoMap.put("description", title);
        swaggerInfoMap.put("version", "1.0.0");
        return swaggerInfoMap;
    }

    protected List<String> prepareSwaggerMapSchemes() {
        return Arrays.asList(getRequest().getScheme());
    }

    protected String derivedBasePath(SwaggerOption swaggerOption) {
        StringBuilder basePath = new StringBuilder();
        basePath.append(getRequest().getContextPath() + "/");
        prepareApplicationVersion().ifPresent(applicationVersion -> {
            basePath.append(applicationVersion + "/");
        });
        return swaggerOption.getDerivedBasePath().map(derivedBasePath -> {
            return derivedBasePath.apply(basePath.toString());
        }).orElse(basePath.toString());
    }

    // ===================================================================================
    //                                                                    Swagger Path Map
    //                                                                    ================
    // e.g. HTML
    // "/signin/signin": {
    //   "post": {
    //     "summary": "@author jflute",
    //     "description": "@author jflute",
    //     "consumes": [
    //       "application/x-www-form-urlencoded"
    //     ],
    //     "parameters": [
    //       {
    //         "name": "account",
    //         "type": "string",
    //         "in": "formData"
    //       },
    //       {
    //         "name": "password",
    //         "type": "string",
    //         "in": "formData"
    //       },
    //       {
    //         "name": "rememberMe",
    //         "type": "boolean",
    //         "in": "formData"
    //       }
    //     ],
    //     "tags": [
    //       "signin"
    //     ],
    //     "responses": {
    //       "200": {
    //         "description": "success",
    //         "schema": {
    //           "type": "object"
    //         }
    //       },
    //       "400": {
    //         "description": "client error"
    //       }
    //     },
    //     "produces": [
    //       "text/html"
    //     ]
    //   },
    //   "parameters": [
    //     {
    //       "in": "header",
    //       "type": "string",
    //       "required": true,
    //       "name": "hangar",
    //       "default": "mystic"
    //     }
    //   ]
    // },
    //
    // e.g. JSON
    // "/signin/": {
    //   "post": {
    //     "summary": "@author jflute",
    //     "description": "@author jflute",
    //     "consumes": [
    //       "application/json"
    //     ],
    //     "parameters": [
    //       {
    //         "name": "SigninBody",
    //         "in": "body",
    //         "required": true,
    //         "schema": {
    //           "$ref": "#/definitions/org.docksidestage.app.web.signin.SigninBody"
    //         }
    //       }
    //     ],
    //     "tags": [
    //       "signin"
    //     ],
    //     "responses": {
    //       "200": {
    //         "description": "success",
    //         "schema": {
    //           "$ref": "#/definitions/org.docksidestage.app.web.signin.SigninResult"
    //         }
    //       },
    //       "400": {
    //         "description": "client error"
    //       }
    //     },
    //     "produces": [
    //       "application/json"
    //     ]
    //   }
    // },
    //
    protected void setupSwaggerPathMap(Map<String, Map<String, Object>> swaggerPathMap // map of top-level paths
            , Map<String, Map<String, Object>> swaggerDefinitionsMap // map of top-level definitions
            , List<Map<String, Object>> swaggerTagList) { // top-level tags
        createActionDocumentGenerator().generateActionDocMetaList().stream().forEach(actiondocMeta -> {
            doSetupSwaggerPathMap(swaggerPathMap, swaggerDefinitionsMap, swaggerTagList, actiondocMeta);
        });
    }

    protected void doSetupSwaggerPathMap(Map<String, Map<String, Object>> swaggerPathMap // map of top-level paths
            , Map<String, Map<String, Object>> swaggerDefinitionsMap // map of top-level definitions
            , List<Map<String, Object>> swaggerTagList // top-level tags
            , ActionDocMeta actionDocMeta) { // document meta of current action
        final String originalActionUrl = actionDocMeta.getUrl();
        // TODO p1us2er0 refactor. (2015/10/09)
        actionDocMeta.setUrl(actionDocMeta.getUrl().replaceAll("\\{\\*", "\\{"));
        final String actionUrl = actionDocMeta.getUrl();

        // arrange swaggerUrlMap in swaggerPathMap if needs
        if (!swaggerPathMap.containsKey(actionUrl)) { // first action for the URL
            final Map<String, Object> swaggerUrlMap = DfCollectionUtil.newLinkedHashMap();
            swaggerPathMap.put(actionUrl, swaggerUrlMap);
        }

        // "/signin/": {
        //   "post": {
        final String httpMethod = extractHttpMethod(actionDocMeta);
        final Map<String, Object> swaggerHttpMethodMap = DfCollectionUtil.newLinkedHashMap();
        swaggerPathMap.get(actionUrl).put(httpMethod, swaggerHttpMethodMap);

        //     "summary": "@author jflute",
        //     "description": "@author jflute",
        swaggerHttpMethodMap.put("summary", actionDocMeta.getDescription());
        swaggerHttpMethodMap.put("description", actionDocMeta.getDescription());

        //     "parameters": [
        //       {
        //         "name": "SigninBody",
        //         "in": "body",
        //         "required": true,
        //         "schema": {
        //           "$ref": "#/definitions/org.docksidestage.app.web.signin.SigninBody"
        //         }
        //       }
        //     ],
        final List<Map<String, Object>> parameterMapList = DfCollectionUtil.newArrayList();
        final List<String> optionalPathNameList = DfCollectionUtil.newArrayList();
        if (actionDocMeta.getFormTypeDocMeta() != null) {
            if (actionDocMeta.getFormTypeDocMeta().getTypeName().endsWith("Form")) {
                //     "consumes": [
                //       "application/x-www-form-urlencoded"
                //     ],
                //     "parameters": [
                //       {
                //         "name": "account",
                //         "type": "string",
                //         "in": "formData"
                //       },
                //       ...
                //     ],
                swaggerHttpMethodMap.put("consumes", Arrays.asList("application/x-www-form-urlencoded"));
                parameterMapList.addAll(actionDocMeta.getFormTypeDocMeta().getNestTypeDocMetaList().stream().map(typeDocMeta -> {
                    final Map<String, Object> parameterMap = toParameterMap(typeDocMeta, swaggerDefinitionsMap);
                    parameterMap.put("name", typeDocMeta.getName());
                    if (actionUrl.contains("{" + typeDocMeta.getName() + "}")) {
                        parameterMap.put("in", "path");
                        // p1us2er0 Swagger path parameters are always required. (2017/10/12)
                        // If path parameter is Option, define Path separately.
                        // https://stackoverflow.com/questions/45549663/swagger-schema-error-should-not-have-additional-properties
                        parameterMap.put("required", true);
                        if (originalActionUrl.contains("{" + typeDocMeta.getName() + "}")) {
                            optionalPathNameList.add(typeDocMeta.getName());
                        }
                    } else {
                        parameterMap.put("in", "get".equals(httpMethod) ? "query" : "formData");
                        if (parameterMap.containsKey("example")) {
                            parameterMap.put("default", parameterMap.get("example"));
                            parameterMap.remove("example");
                        }
                        parameterMap.put("required", typeDocMeta.getAnnotationTypeList().stream().anyMatch(annotationType -> {
                            return getRequiredAnnotationList().stream()
                                    .anyMatch(requiredAnnotation -> requiredAnnotation.isAssignableFrom(annotationType.getClass()));
                        }));
                    }
                    return parameterMap;
                }).collect(Collectors.toList()));
            } else {
                //     "consumes": [
                //       "application/json"
                //     ],
                swaggerHttpMethodMap.put("consumes", Arrays.asList("application/json"));
                final Map<String, Object> parameterMap = DfCollectionUtil.newLinkedHashMap();
                parameterMap.put("name", actionDocMeta.getFormTypeDocMeta().getSimpleTypeName());
                parameterMap.put("in", "body");
                parameterMap.put("required", true);
                final Map<String, Object> schema = DfCollectionUtil.newLinkedHashMap();
                schema.put("type", "object");
                final List<String> requiredPropertyNameList = derivedRequiredPropertyNameList(actionDocMeta.getFormTypeDocMeta());
                if (!requiredPropertyNameList.isEmpty()) {
                    schema.put("required", requiredPropertyNameList);
                }
                schema.put("properties", actionDocMeta.getFormTypeDocMeta().getNestTypeDocMetaList().stream().map(propertyDocMeta -> {
                    return toParameterMap(propertyDocMeta, swaggerDefinitionsMap);
                }).collect(Collectors.toMap(key -> key.get("name"), value -> {
                    final LinkedHashMap<String, Object> propertyMap = DfCollectionUtil.newLinkedHashMap(value);
                    propertyMap.remove("name");
                    return propertyMap;
                }, (u, v) -> v, LinkedHashMap::new)));

                swaggerDefinitionsMap.put(derivedDefinitionName(actionDocMeta.getFormTypeDocMeta()), schema);

                //         "schema": {
                //           "$ref": "#/definitions/org.docksidestage.app.web.signin.SigninBody"
                //         }
                // or
                //         "schema": {
                //           "type": "array",
                //           "items": {
                //             "$ref": "#/definitions/org.docksidestage.app.web.wx.remogen.bean.simple.SuperSimpleBody"
                //           }
                //         }
                LinkedHashMap<String, String> schemaMap =
                        DfCollectionUtil.newLinkedHashMap("$ref", prepareSwaggerMapRefDefinitions(actionDocMeta));
                if (!Iterable.class.isAssignableFrom(actionDocMeta.getFormTypeDocMeta().getType())) {
                    parameterMap.put("schema", schemaMap);
                } else {
                    parameterMap.put("schema", DfCollectionUtil.newLinkedHashMap("type", "array", "items", schemaMap));
                }
                parameterMapList.add(parameterMap);
            }
        }
        // Query, Header, Body, Form
        swaggerHttpMethodMap.put("parameters", parameterMapList);

        //     "tags": [
        //       "signin"
        //     ],
        swaggerHttpMethodMap.put("tags", prepareSwaggerMapTags(actionDocMeta));
        final String tag = DfStringUtil.substringFirstFront(actionUrl.replaceAll("^/", ""), "/");

        // reflect the tags to top-level tags
        if (swaggerTagList.stream().noneMatch(swaggerTag -> swaggerTag.containsValue(tag))) {
            swaggerTagList.add(DfCollectionUtil.newLinkedHashMap("name", tag));
        }

        //     "responses": {
        //       ...
        prepareSwaggerMapResponseMap(swaggerHttpMethodMap, actionDocMeta, swaggerDefinitionsMap);

        if (!optionalPathNameList.isEmpty()) {
            doSetupSwaggerPathMapForOptionalPath(swaggerPathMap, actionDocMeta, optionalPathNameList);
        }
    }

    protected void doSetupSwaggerPathMapForOptionalPath(Map<String, Map<String, Object>> swaggerPathMap, ActionDocMeta actionDocMeta,
            List<String> optionalPathNameList) {
        final String actionUrl = actionDocMeta.getUrl();
        final String httpMethod = extractHttpMethod(actionDocMeta);
        RealJsonEngine jsonEngine = createJsonEngine();
        String json = jsonEngine.toJson(swaggerPathMap.get(actionUrl).get(httpMethod));

        IntStream.range(0, optionalPathNameList.size()).forEach(index -> {
            List<String> deleteOptionalPathNameList = optionalPathNameList.subList(index, optionalPathNameList.size());
            String deleteOptionalPathNameUrl = deleteOptionalPathNameList.stream().reduce(actionUrl, (aactionUrl, optionalPathName) -> {
                return aactionUrl.replaceAll("/\\{" + optionalPathName + "\\}", "");
            });
            // arrange swaggerUrlMap in swaggerPathMap if needs
            if (!swaggerPathMap.containsKey(deleteOptionalPathNameUrl)) { // first action for the URL
                swaggerPathMap.put(deleteOptionalPathNameUrl, DfCollectionUtil.newLinkedHashMap());
            }
            Map<String, Object> swaggerHttpMethodMap = jsonEngine.fromJsonParameteried(json, new ParameterizedRef<Map<String, Object>>() {
            }.getType());
            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> parameterMapList =
                    ((List<Map<String, Object>>) swaggerHttpMethodMap.get("parameters")).stream().filter(parameter -> {
                        return !deleteOptionalPathNameList.contains(parameter.get("name"));
                    }).collect(Collectors.toList());
            swaggerHttpMethodMap.put("parameters", parameterMapList);
            swaggerPathMap.get(deleteOptionalPathNameUrl).put(httpMethod, swaggerHttpMethodMap);
        });
        Map<String, Object> swaggerUrlMap = swaggerPathMap.remove(actionUrl);
        swaggerPathMap.put(actionUrl, swaggerUrlMap);
    }

    protected String extractHttpMethod(ActionDocMeta actionDocMeta) {
        if (actionDocMeta.getFormTypeDocMeta() == null) {
            return "get";
        }
        boolean jsonParameterExist = actionDocMeta.getFormTypeDocMeta().getNestTypeDocMetaList().stream().anyMatch(typeDocMeta -> {
            return typeDocMeta.getAnnotationTypeList().stream().anyMatch(annotationType -> {
                return JsonParameter.class.isAssignableFrom(annotationType.getClass());
            });
        });
        return jsonParameterExist ? "post" : getDefaultHttpMethodWhenFormExists();
    }

    protected String getDefaultHttpMethodWhenFormExists() {
        return "get";
    }

    protected String prepareSwaggerMapRefDefinitions(ActionDocMeta actiondocMeta) {
        return "#/definitions/" + derivedDefinitionName(actiondocMeta.getFormTypeDocMeta());
    }

    protected List<String> prepareSwaggerMapTags(ActionDocMeta actiondocMeta) {
        return Arrays.asList(DfStringUtil.substringFirstFront(actiondocMeta.getUrl().replaceAll("^/", ""), "/"));
    }

    protected void prepareSwaggerMapResponseMap(Map<String, Object> swaggerHttpMethodMap, ActionDocMeta actiondocMeta,
            Map<String, Map<String, Object>> swaggerDefinitionsMap) {
        //     "responses": {
        //       "200": {
        //         "description": "success",
        //         "schema": {
        //           "$ref": "#/definitions/org.docksidestage.app.web.signin.SigninResult"
        //         }
        //       },
        //       "400": {
        //         "description": "client error"
        //       }
        //     },
        final Map<String, Object> responseMap = DfCollectionUtil.newLinkedHashMap();
        swaggerHttpMethodMap.put("responses", responseMap);
        derivedProduces(actiondocMeta).ifPresent(produces -> {
            swaggerHttpMethodMap.put("produces", produces);
        });
        final Map<String, Object> response = DfCollectionUtil.newLinkedHashMap("description", "success");
        final TypeDocMeta returnTypeDocMeta = actiondocMeta.getReturnTypeDocMeta();
        if (!returnTypeDocMeta.getType().equals(String.class)) {
            if (!Arrays.asList(void.class, Void.class).contains(returnTypeDocMeta.getGenericType())) {
                final Map<String, Object> parameterMap = toParameterMap(returnTypeDocMeta, swaggerDefinitionsMap);
                parameterMap.remove("name");
                parameterMap.remove("required");
                if (parameterMap.containsKey("schema")) {
                    response.putAll(parameterMap);
                } else {
                    response.put("schema", parameterMap);
                }
            }
        }
        responseMap.put("200", response);
        responseMap.put("400", DfCollectionUtil.newLinkedHashMap("description", "client error"));
    }

    // ===================================================================================
    //                                                          Swagger Map Option Element
    //                                                          ==========================
    protected void adaptSecurityDefinitions(Map<String, Object> swaggerMap, List<Map<String, Object>> securityDefinitionList) {
        final Map<Object, Object> securityDefinitions = DfCollectionUtil.newLinkedHashMap();
        final Map<Object, Object> security = DfCollectionUtil.newLinkedHashMap();
        swaggerMap.put("securityDefinitions", securityDefinitions);
        swaggerMap.put("security", security);
        securityDefinitionList.forEach(securityDefinition -> {
            securityDefinitions.put(securityDefinition.get("name"), securityDefinition);
            security.put(securityDefinition.get("name"), Arrays.asList());
        });
    }

    protected void adaptHeaderParameters(Map<String, Object> swaggerMap, List<Map<String, Object>> headerParameterList) {
        if (headerParameterList.isEmpty()) {
            return;
        }
        final Object paths = swaggerMap.get("paths");
        if (!(paths instanceof Map<?, ?>)) {
            return;
        }
        @SuppressWarnings("unchecked")
        final Map<Object, Object> pathMap = (Map<Object, Object>) paths;
        pathMap.forEach((path, pathData) -> {
            if (!(pathData instanceof Map<?, ?>)) {
                return;
            }
            @SuppressWarnings("unchecked")
            final Map<Object, Object> pathDataMap = (Map<Object, Object>) pathData;

            headerParameterList.forEach(headerParameter -> {
                if (!pathDataMap.containsKey("parameters")) {
                    pathDataMap.put("parameters", DfCollectionUtil.newArrayList());
                }
                final Object parameters = pathDataMap.get("parameters");
                if (parameters instanceof List<?>) {
                    @SuppressWarnings("all")
                    final List<Object> parameterList = (List<Object>) parameters;
                    parameterList.add(headerParameter);
                }
            });
        });
    }

    // ===================================================================================
    //                                                                       Parameter Map
    //                                                                       =============
    protected Map<String, Object> toParameterMap(TypeDocMeta typeDocMeta, Map<String, Map<String, Object>> definitionsMap) {
        final Map<Class<?>, Tuple3<String, String, BiFunction<TypeDocMeta, Object, Object>>> typeMap = createTypeMap();
        final Class<?> keepType = typeDocMeta.getType();
        if (typeDocMeta.getGenericType() != null && (ActionResponse.class.isAssignableFrom(typeDocMeta.getType())
                || OptionalThing.class.isAssignableFrom(typeDocMeta.getType()))) {
            typeDocMeta.setType(typeDocMeta.getGenericType());
        }

        final Map<String, Object> parameterMap = DfCollectionUtil.newLinkedHashMap();
        parameterMap.put("name", typeDocMeta.getPublicName());
        if (DfStringUtil.is_NotNull_and_NotEmpty(typeDocMeta.getDescription())) {
            parameterMap.put("description", typeDocMeta.getDescription());
        }
        if (typeMap.containsKey(typeDocMeta.getType())) {
            final Tuple3<String, String, BiFunction<TypeDocMeta, Object, Object>> swaggerType = typeMap.get(typeDocMeta.getType());
            parameterMap.put("type", swaggerType.getValue1());
            final String format = swaggerType.getValue2();
            if (DfStringUtil.is_NotNull_and_NotEmpty(format)) {
                parameterMap.put("format", format);
            }
        } else if (Iterable.class.isAssignableFrom(typeDocMeta.getType())) {
            setupBeanList(typeDocMeta, definitionsMap, typeMap, parameterMap);
        } else if (typeDocMeta.getType().equals(Object.class) || Map.class.isAssignableFrom(typeDocMeta.getType())) {
            parameterMap.put("type", "object");
        } else if (!typeDocMeta.getNestTypeDocMetaList().isEmpty()) {
            String definition = putDefinition(definitionsMap, typeDocMeta);
            parameterMap.put("$ref", definition);
        } else {
            parameterMap.put("type", "object");
            try {
                final Class<?> clazz = DfReflectionUtil.forName(typeDocMeta.getTypeName());
                if (Enum.class.isAssignableFrom(clazz)) {
                    parameterMap.put("type", "string");
                    @SuppressWarnings("unchecked")
                    final Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) clazz;
                    final List<Map<String, String>> enumMap = buildEnumMapList(enumClass);
                    parameterMap.put("enum", enumMap.stream().map(e -> e.get("code")).collect(Collectors.toList()));
                    String description = typeDocMeta.getDescription();
                    if (DfStringUtil.is_Null_or_Empty(description)) {
                        description = typeDocMeta.getPublicName();
                    }
                    description += ":" + enumMap.stream().map(e -> {
                        return String.format(" * `%s` - %s, %s.", e.get("code"), e.get("name"), e.get("alias"));
                    }).collect(Collectors.joining());
                    parameterMap.put("description", description);
                }
            } catch (RuntimeException ignored) {}
        }

        typeDocMeta.getAnnotationTypeList().forEach(annotation -> {
            if (annotation instanceof Minlength) {
                final Minlength size = (Minlength) annotation;
                parameterMap.put("minimum", size.minlength());
            }
            if (annotation instanceof Maxlength) {
                final Maxlength size = (Maxlength) annotation;
                parameterMap.put("maximum", size.maxlength());
            }
            if (annotation instanceof Minbytelength) {
                final Minbytelength size = (Minbytelength) annotation;
                parameterMap.put("minimum", size.minbytelength());
            }
            if (annotation instanceof Maxbytelength) {
                final Maxbytelength size = (Maxbytelength) annotation;
                parameterMap.put("maximum", size.maxbytelength());
            }
            // pattern, maxItems, minItems
        });

        deriveDefaultValue(typeDocMeta).ifPresent(defaultValue -> {
            parameterMap.put("example", defaultValue);
        });

        typeDocMeta.setType(keepType);
        return parameterMap;
    }

    protected void setupBeanList(TypeDocMeta typeDocMeta, Map<String, Map<String, Object>> definitionsMap,
            Map<Class<?>, Tuple3<String, String, BiFunction<TypeDocMeta, Object, Object>>> typeMap, Map<String, Object> schemaMap) {
        schemaMap.put("type", "array");
        if (!typeDocMeta.getNestTypeDocMetaList().isEmpty()) {
            final String definition = putDefinition(definitionsMap, typeDocMeta);
            schemaMap.put("items", DfCollectionUtil.newLinkedHashMap("$ref", definition));
        } else {
            final Map<String, String> items = DfCollectionUtil.newLinkedHashMap();
            final Class<?> genericType = typeDocMeta.getGenericType();
            if (genericType != null) {
                final Tuple3<String, String, BiFunction<TypeDocMeta, Object, Object>> swaggerType = typeMap.get(genericType);
                if (swaggerType != null) {
                    items.put("type", swaggerType.getValue1());
                    final String format = swaggerType.getValue2();
                    if (DfStringUtil.is_NotNull_and_NotEmpty(format)) {
                        items.put("format", format);
                    }
                }
            }
            if (!items.containsKey("type")) {
                items.put("type", "object");
            }
            schemaMap.put("items", items);
        }
        if (typeDocMeta.getSimpleTypeName().matches(".*List<.*List<.*")) {
            schemaMap.put("items", DfCollectionUtil.newLinkedHashMap("type", "array", "items", schemaMap.get("items")));
        }
    }

    protected String putDefinition(Map<String, Map<String, Object>> definitionsMap, TypeDocMeta typeDocMeta) {
        final Map<String, Object> schema = DfCollectionUtil.newLinkedHashMap();
        schema.put("type", "object");
        final List<String> requiredPropertyNameList = derivedRequiredPropertyNameList(typeDocMeta);
        if (!requiredPropertyNameList.isEmpty()) {
            schema.put("required", requiredPropertyNameList);
        }
        schema.put("properties", typeDocMeta.getNestTypeDocMetaList().stream().map(nestTypeDocMeta -> {
            return toParameterMap(nestTypeDocMeta, definitionsMap);
        }).collect(Collectors.toMap(key -> key.get("name"), value -> {
            // TODO p1us2er0 remove name. refactor required. (2017/10/12)
            final LinkedHashMap<String, Object> property = DfCollectionUtil.newLinkedHashMap(value);
            property.remove("name");
            return property;
        }, (u, v) -> v, LinkedHashMap::new)));
        definitionsMap.put(derivedDefinitionName(typeDocMeta), schema);
        return "#/definitions/" + derivedDefinitionName(typeDocMeta);
    }

    protected List<String> derivedRequiredPropertyNameList(TypeDocMeta typeDocMeta) {
        return typeDocMeta.getNestTypeDocMetaList().stream().filter(nesttypeDocMeta -> {
            return nesttypeDocMeta.getAnnotationTypeList().stream().anyMatch(annotationType -> {
                return getRequiredAnnotationList().stream()
                        .anyMatch(requiredAnnotation -> requiredAnnotation.isAssignableFrom(annotationType.getClass()));
            });
        }).map(nesttypeDocMeta -> nesttypeDocMeta.getPublicName()).collect(Collectors.toList());
    }

    protected String derivedDefinitionName(TypeDocMeta typeDocMeta) {
        if (typeDocMeta.getTypeName().matches("^[^<]+<(.+)>$")) {
            return typeDocMeta.getTypeName().replaceAll("^[^<]+<(.+)>$", "$1");
        }
        return typeDocMeta.getTypeName();
    }

    protected OptionalThing<List<String>> derivedProduces(ActionDocMeta actiondocMeta) {
        if (Arrays.asList(void.class, Void.class).contains(actiondocMeta.getReturnTypeDocMeta().getGenericType())) {
            return OptionalThing.empty();
        }
        if (createTypeMap().containsKey(actiondocMeta.getReturnTypeDocMeta().getGenericType())) {
            return OptionalThing.of(Arrays.asList("text/plain;charset=UTF-8"));
        }
        final Map<Class<?>, List<String>> produceMap = DfCollectionUtil.newHashMap();
        produceMap.put(JsonResponse.class, Arrays.asList("application/json"));
        produceMap.put(XmlResponse.class, Arrays.asList("application/xml"));
        produceMap.put(String.class, Arrays.asList("text/html"));
        produceMap.put(StreamResponse.class, Arrays.asList("application/octet-stream"));
        final Class<?> produceType = actiondocMeta.getReturnTypeDocMeta().getType();
        return OptionalThing.migratedFrom(produceMap.entrySet().stream().filter(produce -> produce.getKey().isAssignableFrom(produceType))
                .findFirst().map(produce -> produce.getValue()), () -> {
                    String msg = "Not found the produce: type=" + produceType + ", keys=" + produceMap.keySet();
                    throw new IllegalStateException(msg);
                });
    }

    protected Map<Class<?>, Tuple3<String, String, BiFunction<TypeDocMeta, Object, Object>>> createTypeMap() {
        final Map<Class<?>, Tuple3<String, String, BiFunction<TypeDocMeta, Object, Object>>> typeMap = DfCollectionUtil.newLinkedHashMap();
        typeMap.put(boolean.class, Tuple3.tuple3("boolean", null, (typeDocMeta, value) -> DfTypeUtil.toBoolean(value)));
        typeMap.put(byte.class, Tuple3.tuple3("byte", null, (typeDocMeta, value) -> DfTypeUtil.toByte(value)));
        typeMap.put(int.class, Tuple3.tuple3("integer", "int32", (typeDocMeta, value) -> DfTypeUtil.toInteger(value)));
        typeMap.put(long.class, Tuple3.tuple3("integer", "int64", (typeDocMeta, value) -> DfTypeUtil.toLong(value)));
        typeMap.put(float.class, Tuple3.tuple3("integer", "float", (typeDocMeta, value) -> DfTypeUtil.toFloat(value)));
        typeMap.put(double.class, Tuple3.tuple3("integer", "double", (typeDocMeta, value) -> DfTypeUtil.toDouble(value)));
        typeMap.put(Boolean.class, Tuple3.tuple3("boolean", null, (typeDocMeta, value) -> DfTypeUtil.toBoolean(value)));
        typeMap.put(Byte.class, Tuple3.tuple3("boolean", null, (typeDocMeta, value) -> DfTypeUtil.toByte(value)));
        typeMap.put(Integer.class, Tuple3.tuple3("integer", "int32", (typeDocMeta, value) -> DfTypeUtil.toInteger(value)));
        typeMap.put(Long.class, Tuple3.tuple3("integer", "int64", (typeDocMeta, value) -> DfTypeUtil.toLong(value)));
        typeMap.put(Float.class, Tuple3.tuple3("integer", "float", (typeDocMeta, value) -> DfTypeUtil.toFloat(value)));
        typeMap.put(Double.class, Tuple3.tuple3("integer", "double", (typeDocMeta, value) -> DfTypeUtil.toDouble(value)));
        typeMap.put(String.class, Tuple3.tuple3("string", null, (typeDocMeta, value) -> value));
        typeMap.put(byte[].class, Tuple3.tuple3("string", "byte", (typeDocMeta, value) -> value));
        typeMap.put(Byte[].class, Tuple3.tuple3("string", "byte", (typeDocMeta, value) -> value));
        typeMap.put(Date.class, Tuple3.tuple3("string", "date", (typeDocMeta, value) -> {
            return value == null ? getLocalDateFormatter(typeDocMeta).format(getDefaultLocalDate()) : value;
        }));
        typeMap.put(LocalDate.class, Tuple3.tuple3("string", "date", (typeDocMeta, value) -> {
            return value == null ? getLocalDateFormatter(typeDocMeta).format(getDefaultLocalDate()) : value;
        }));
        typeMap.put(LocalDateTime.class, Tuple3.tuple3("string", "date-time", (typeDocMeta, value) -> {
            return value == null ? getLocalDateTimeFormatter(typeDocMeta).format(getDefaultLocalDateTime()) : value;
        }));
        typeMap.put(LocalTime.class, Tuple3.tuple3("string", null, (typeDocMeta, value) -> {
            return value == null ? getLocalTimeFormatter(typeDocMeta).format(getDefaultLocalTime()) : value;
        }));
        typeMap.put(FormFile.class, Tuple3.tuple3("binary", null, (typeDocMeta, value) -> value));
        return typeMap;
    }

    protected List<Class<? extends Annotation>> getRequiredAnnotationList() {
        return Arrays.asList(Required.class);
    }

    protected List<Map<String, String>> buildEnumMapList(Class<? extends Enum<?>> typeClass) {
        // cannot resolve type by maven compiler, explicitly cast it
        final List<Map<String, String>> enumMapList = Arrays.stream(typeClass.getEnumConstants()).map(enumConstant -> {
            Map<String, String> map = DfCollectionUtil.newLinkedHashMap("name", enumConstant.name());
            if (enumConstant instanceof Classification) {
                map.put("code", ((Classification) enumConstant).code());
                map.put("alias", ((Classification) enumConstant).alias());
            } else {
                map.put("code", enumConstant.name());
                map.put("alias", "");
            }
            return map;
        }).collect(Collectors.toList());
        return enumMapList;
    }

    // ===================================================================================
    //                                                                  Document Generator
    //                                                                  ==================
    protected DocumentGenerator createDocumentGenerator() {
        return new DocumentGenerator();
    }

    protected DocumentGeneratorFactory createDocumentGeneratorFactory() {
        return new DocumentGeneratorFactory();
    }

    protected ActionDocumentGenerator createActionDocumentGenerator() {
        return createDocumentGenerator().createActionDocumentGenerator();
    }

    protected OptionalThing<String> prepareApplicationVersion() {
        return OptionalThing.empty();
    }

    protected LocalDate getDefaultLocalDate() {
        return LocalDate.ofYearDay(2000, 1);
    }

    protected LocalDateTime getDefaultLocalDateTime() {
        return getDefaultLocalDate().atStartOfDay();
    }

    protected LocalTime getDefaultLocalTime() {
        return LocalTime.from(getDefaultLocalDateTime());
    }

    protected DateTimeFormatter getLocalDateFormatter(TypeDocMeta typeDocMeta) {
        Optional<DateTimeFormatter> jsonDatePatternDateTimeFormatter = getJsonDatePatternDateTimeFormatter(typeDocMeta);
        if (jsonDatePatternDateTimeFormatter.isPresent()) {
            return jsonDatePatternDateTimeFormatter.get();
        }
        return getApplicationJsonMappingOption()
                .flatMap(applicationJsonMappingOption -> applicationJsonMappingOption.getLocalDateFormatter())
                .orElseGet(() -> DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    protected DateTimeFormatter getLocalDateTimeFormatter(TypeDocMeta typeDocMeta) {
        Optional<DateTimeFormatter> jsonDatePatternDateTimeFormatter = getJsonDatePatternDateTimeFormatter(typeDocMeta);
        if (jsonDatePatternDateTimeFormatter.isPresent()) {
            return jsonDatePatternDateTimeFormatter.get();
        }
        return getApplicationJsonMappingOption()
                .flatMap(applicationJsonMappingOption -> applicationJsonMappingOption.getLocalDateTimeFormatter())
                .orElseGet(() -> DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }

    protected DateTimeFormatter getLocalTimeFormatter(TypeDocMeta typeDocMeta) {
        Optional<DateTimeFormatter> jsonDatePatternDateTimeFormatter = getJsonDatePatternDateTimeFormatter(typeDocMeta);
        if (jsonDatePatternDateTimeFormatter.isPresent()) {
            return jsonDatePatternDateTimeFormatter.get();
        }
        return getApplicationJsonMappingOption()
                .flatMap(applicationJsonMappingOption -> applicationJsonMappingOption.getLocalTimeFormatter())
                .orElseGet(() -> DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }

    protected Optional<DateTimeFormatter> getJsonDatePatternDateTimeFormatter(TypeDocMeta typeDocMeta) {
        return typeDocMeta.getAnnotationTypeList()
                .stream()
                .filter(annotationType -> annotationType instanceof JsonDatePattern)
                .findFirst()
                .map(jsonDatePattern -> {
                    return DateTimeFormatter.ofPattern(((JsonDatePattern) jsonDatePattern).value());
                });
    }

    protected OptionalThing<Object> deriveDefaultValue(TypeDocMeta typeDocMeta) {
        Map<Class<?>, Tuple3<String, String, BiFunction<TypeDocMeta, Object, Object>>> typeMap = createTypeMap();
        if (typeMap.containsKey(typeDocMeta.getType())) {
            Tuple3<String, String, BiFunction<TypeDocMeta, Object, Object>> swaggerType = typeMap.get(typeDocMeta.getType());
            Object defaultValue = swaggerType.getValue3().apply(typeDocMeta, deriveDefaultValueByComment(typeDocMeta.getComment()));
            if (defaultValue != null) {
                return OptionalThing.of(defaultValue);
            }
        } else if (Iterable.class.isAssignableFrom(typeDocMeta.getType()) && typeDocMeta.getNestTypeDocMetaList().isEmpty()) {
            Object defaultValue = deriveDefaultValueByComment(typeDocMeta.getComment());
            if (!(defaultValue instanceof List)) {
                return OptionalThing.empty();
            }
            @SuppressWarnings("unchecked")
            List<Object> defaultValueList = (List<Object>) defaultValue;
            Class<?> genericType = typeDocMeta.getGenericType();
            if (genericType == null) {
                genericType = String.class;
            }
            Tuple3<String, String, BiFunction<TypeDocMeta, Object, Object>> swaggerType = typeMap.get(genericType);
            if (swaggerType != null) {
                return OptionalThing.of(defaultValueList.stream().map(value -> {
                    return swaggerType.getValue3().apply(typeDocMeta, value);
                }).collect(Collectors.toList()));
            }
        } else if (Enum.class.isAssignableFrom(typeDocMeta.getType())) {
            Object defaultValue = deriveDefaultValueByComment(typeDocMeta.getComment());
            if (defaultValue != null) {
                return OptionalThing.of(defaultValue);
            } else {
                @SuppressWarnings("unchecked")
                Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) typeDocMeta.getType();
                List<Map<String, String>> enumMapList = buildEnumMapList(enumClass);
                return OptionalThing.migratedFrom(enumMapList.stream().map(e -> (Object) e.get("code")).findFirst(), () -> {
                    throw new IllegalStateException("not found enum value.");
                });
            }
        }
        return OptionalThing.empty();
    }

    protected Object deriveDefaultValueByComment(String comment) {
        if (DfStringUtil.is_NotNull_and_NotEmpty(comment)) {
            String commentWithoutLine = comment.replaceAll("\r?\n", " ");
            if (commentWithoutLine.contains(" e.g. \"")) {
                return DfStringUtil.substringFirstFront(DfStringUtil.substringFirstRear(commentWithoutLine, " e.g. \""), "\"");
            }
            if (commentWithoutLine.contains(" e.g. [")) {
                String defaultValue = DfStringUtil.substringFirstFront(DfStringUtil.substringFirstRear(commentWithoutLine, " e.g. ["), "]");
                return Arrays.stream(defaultValue.split(", *")).map(value -> {
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        return value.substring(1, value.length() - 1);
                    }
                    return "null".equals(value) ? null : value;
                }).collect(Collectors.toList());
            }
            Pattern pattern = Pattern.compile(" e\\.g\\. ([^ ]+)");
            Matcher matcher = pattern.matcher(commentWithoutLine);
            if (matcher.find()) {
                String value = matcher.group(1);
                return "null".equals(value) ? null : value;
            }
        }
        return null;
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected String getLastaDocDir() {
        return createDocumentGeneratorFactory().getLastaDocDir();
    }

    protected AccessibleConfig getAccessibleConfig() {
        return ContainerUtil.getComponent(AccessibleConfig.class);
    }

    protected HttpServletRequest getRequest() {
        return RequestUtil.getRequest();
    }

    protected RealJsonEngine createJsonEngine() {
        return createDocumentGeneratorFactory().createJsonEngine();
    }

    protected OptionalThing<JsonMappingOption> getApplicationJsonMappingOption() {
        return createDocumentGeneratorFactory().getApplicationJsonMappingOption();
    }
}
