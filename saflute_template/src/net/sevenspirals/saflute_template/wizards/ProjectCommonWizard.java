package net.sevenspirals.saflute_template.wizards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import net.sevenspirals.saflute_template.Activator;
import net.sevenspirals.saflute_template.pages.SAFluteCommonProjectCreationPage;
import net.sevenspirals.saflute_template.pages.SAFluteCommonTemplateArtifactPage;
import net.sevenspirals.saflute_template.wizards.base.AbstractSAFluteTemplateWizard;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.osgi.framework.Bundle;

public class ProjectCommonWizard extends AbstractSAFluteTemplateWizard implements INewWizard {
    private IPackageFragment mainJavaSourceRoot;
    private IPackageFragment testJavaSourceRoot;

    public ProjectCommonWizard() {
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        template_root = "templates/template-common";
    }

    @Override
    public boolean performFinish() {
        IProject project = page.getProjectHandle();

        try {
            project.create(null);
            project.open(null);

            IJavaProject javaProject = JavaCore.create(project);

            setupVelocity(javaProject.getProject());

            createSourceFolders(javaProject);

            Bundle bundle = Activator.getDefault().getBundle();
            try {
                URL url = FileLocator.toFileURL(bundle.getEntry(template_root));
                File rootFolder = new File(url.getFile());
                URL url2 = FileLocator.toFileURL(bundle.getEntry(template_root + "/src/main/java/dummy"));
                File mainJavaFolder = new File(url2.getFile());
                URL url3 = FileLocator.toFileURL(bundle.getEntry(template_root + "/src/test/java/dummy"));
                File testJavaFolder = new File(url3.getFile());

                try {
                    copyFiles(rootFolder, javaProject.getProject());
                    copyFiles(mainJavaFolder, ((IFolder) mainJavaSourceRoot.getResource()));
                    copyFiles(testJavaFolder, ((IFolder) testJavaSourceRoot.getResource()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void createSourceFolders(IJavaProject javaProject) throws CoreException {
        IFolder src = javaProject.getProject().getFolder("src");
        src.create(true, true, null);

        mainJavaSourceRoot = createSourceFolder(javaProject, "main");
        testJavaSourceRoot = createSourceFolder(javaProject, "test");
    }

    @SuppressWarnings("restriction")
    private IPackageFragment createSourceFolder(IJavaProject javaProject, String folderName) throws CoreException,
            JavaModelException {
        IFolder srcFolder = javaProject.getProject().getFolder("src/" + folderName);
        srcFolder.create(true, true, null);
        IFolder java = javaProject.getProject().getFolder("src/" + folderName + "/java");
        java.create(true, true, null);
        IFolder resources = javaProject.getProject().getFolder("src/" + folderName + "/resources");
        resources.create(true, true, null);

        javaProject.getPackageFragmentRoot(resources);
        // packageを作成
        return javaProject.getPackageFragmentRoot(java).createPackageFragment(artifactPage.getArtifact().getGroupId(),
                true, null);
    }

    @Override
    public void addPages() {
        artifactPage = new SAFluteCommonTemplateArtifactPage(new ProjectImportConfiguration());
        page = new SAFluteCommonProjectCreationPage("page", artifactPage);
        this.addPage(artifactPage);
        this.addPage(page);
        super.addPages();
    }
}
