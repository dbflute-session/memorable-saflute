package net.sevenspirals.saflute_template.wizards.base;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import net.sevenspirals.saflute_template.dto.EclipseTemplate;
import net.sevenspirals.saflute_template.pages.base.SAFluteTemplateArtifactPageBase;
import net.sevenspirals.saflute_template.pages.base.SAFluteTemplateProjectCreationPageBase;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.Wizard;

/**
 * @author syaku
 * @author jflute
 */
public abstract class AbstractSAFluteTemplateWizard extends Wizard {

    protected String template_root;

    protected String ProjectName;
    protected String projectname;
    protected String AppName;
    protected String appname;

    protected SAFluteTemplateArtifactPageBase artifactPage;
    protected SAFluteTemplateProjectCreationPageBase page;
    protected VelocityEngine engine;
    protected VelocityContext context;

    public static Set<String> ignoreList = new HashSet<String>(Arrays.asList("java-editor-templates.xml"));
    public static Set<String> extensions = new HashSet<String>(Arrays.asList("xml", "project", "dfprop", "txt",
            "properties", "dicon", "java", "erm"));

    public static String GetExtension(String prmFileName) {
        int point = prmFileName.lastIndexOf(".");
        String ExtensionName = "";
        try {
            if (point != -1) {
                ExtensionName = prmFileName.substring(point + 1);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + prmFileName + "でエラー");
        }
        return ExtensionName;
    }

    @SuppressWarnings("restriction")
    protected void setupVelocity(IProject project) {
        EclipseTemplate eclipse = new EclipseTemplate();
        eclipse.setProjectName(project.getName());

        // not to use org.eclipse.osgi.internal.framework.ContextFinder
        // unknown following error at Eclipse-4.4, it may be class loader problem
        // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
        // org.apache.velocity.exception.VelocityException:
        //  The specified class for ResourceManager (org.apache.velocity.runtime.resource.ResourceManagerImpl)
        //   does not implement org.apache.velocity.runtime.resource.ResourceManager;
        //   Velocity is not initialized correctly.
        // - - - - - - - - - -/
        Thread.currentThread().setContextClassLoader(null);

        Properties p = new Properties();
        p.setProperty("input.encoding", "UTF-8");
        p.setProperty("output.encoding", "UTF-8");
        Velocity.init(p);
        context = new VelocityContext();
        context.put("_DS_", "##");
        context.put("artifactId", artifactPage.getArtifact().getArtifactId());
        context.put("groupId", artifactPage.getArtifact().getGroupId());
        context.put("version", artifactPage.getArtifact().getVersion());
        context.put("description", artifactPage.getArtifact().getDescription());
        context.put("packaging", artifactPage.getArtifact().getPackaging());
        context.put("baseGroup", artifactPage.getParentArtifact().getGroupIdCombo().getText());
        context.put("baseProject", artifactPage.getParentArtifact().getArtifactIdCombo().getText());
        context.put("parentVersion", artifactPage.getParentArtifact().getVersionCombo().getText());
        context.put("packageName", artifactPage.getArtifact().getGroupId());
        context.put("ProjectName", page.getSAFluteProjectName());
        context.put("PROJECTNAME", StringUtils.upperCase(page.getSAFluteProjectName()));
        context.put("projectname", StringUtils.lowerCase(page.getSAFluteProjectName()));
        context.put("AppName", page.getAppName());
        context.put("APPNAME", StringUtils.upperCase(page.getAppName()));
        context.put("appname", StringUtils.lowerCase(page.getAppName()));
        context.put("eclipse", eclipse);

        ProjectName = page.getSAFluteProjectName();
        projectname = StringUtils.lowerCase(page.getSAFluteProjectName());
        AppName = page.getAppName();
        appname = StringUtils.lowerCase(page.getAppName());
    }

    protected void copyFiles(File srcFolder, IContainer destFolder) throws FileNotFoundException, CoreException {
        for (File f : srcFolder.listFiles()) {
            String fileName = f.getName();

            fileName = fileName.replaceAll("\\_ProjectName\\_", ProjectName);
            fileName = fileName.replaceAll("\\_projectname\\_", projectname);
            fileName = fileName.replaceAll("\\_AppName\\_", AppName);
            fileName = fileName.replaceAll("\\_appname\\_", appname);
            fileName = fileName.replaceAll("\\_dot\\_", "."); // for e.g. .gitignore

            if (f.isDirectory()) {
                // ソースはスキップ
                if (!"java".equals(fileName)) {
                    IFolder newDest = destFolder.getFolder(new Path(fileName));
                    if (!newDest.exists())
                        newDest.create(true, true, null);
                    copyFiles(f, newDest);
                }
            } else {
                if (!ignoreList.contains(f.getName()) && extensions.contains(GetExtension(f.getAbsolutePath()))) {
                    if ("vm".equals(GetExtension(f.getAbsolutePath())))
                        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                    IFile newFile = destFolder.getFile(new Path(fileName));
                    StringWriter writer = new StringWriter();

                    try {
                    	Velocity.evaluate(context, writer, "project base pom.xml", new InputStreamReader(new FileInputStream(f), "UTF-8"));
                        if (newFile.exists()) {
                            newFile.setContents(new ByteArrayInputStream(writer.toString().getBytes("UTF-8")), 0, null);
                        } else {
                            newFile.create(new ByteArrayInputStream(writer.toString().getBytes("UTF-8")), true, null);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    IFile newFile = destFolder.getFile(new Path(fileName));
                    if (newFile.exists()) {
                        newFile.setContents(new FileInputStream(f), 0, null);
                    } else {
                        newFile.create(new FileInputStream(f), true, null);
                    }
                }
            }
        }
    }
}
