package net.sevenspirals.saflute_template.wizards;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import net.sevenspirals.saflute_template.Activator;
import net.sevenspirals.saflute_template.pages.SAFluteBaseProjectCreationPage;
import net.sevenspirals.saflute_template.pages.SAFluteBaseTemplateArtifactPage;
import net.sevenspirals.saflute_template.wizards.base.AbstractSAFluteTemplateWizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.osgi.framework.Bundle;

public class ProjectBaseWizard extends AbstractSAFluteTemplateWizard implements INewWizard {

    public ProjectBaseWizard() {
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        template_root = "templates/template-base";
    }

    @Override
    public boolean performFinish() {

        IProject project = page.getProjectHandle();

        try {
            project.create(null);
        } catch (CoreException e) {
            e.printStackTrace();
        }

        try {
            project.open(null);
        } catch (CoreException e) {
            e.printStackTrace();
        }

        Bundle bundle = Activator.getDefault().getBundle();
        URL url;
        try {
            url = FileLocator.toFileURL(bundle.getEntry(template_root));
            File srcFile = new File(url.getFile());

            setupVelocity(project);

            try {
                this.copyFiles(srcFile, project);
            } catch (CoreException e) {
                e.printStackTrace();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return true;
    }

    @Override
    public void addPages() {
        artifactPage = new SAFluteBaseTemplateArtifactPage(new ProjectImportConfiguration());
        page = new SAFluteBaseProjectCreationPage("page", artifactPage);
        this.addPage(artifactPage);
        this.addPage(page);
        super.addPages();
    }

}
