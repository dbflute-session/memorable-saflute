package net.sevenspirals.saflute_template.wizards;

import net.sevenspirals.saflute_template.pages.SAFluteApplicationTemplateProjectArtifactPage;
import net.sevenspirals.saflute_template.pages.SAFluteApplicationProjectCreationPage;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class ProjectApplicationWizard extends ProjectCommonWizard implements INewWizard {

    public ProjectApplicationWizard() {
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        super.init(workbench, selection);
        this.template_root = "templates/template-application";
    }

    @Override
    public void addPages() {
        artifactPage = new SAFluteApplicationTemplateProjectArtifactPage(new ProjectImportConfiguration());
        page = new SAFluteApplicationProjectCreationPage("page", artifactPage);
        this.addPage(artifactPage);
        this.addPage(page);
    }
}
