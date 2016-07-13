package net.sevenspirals.saflute_template.pages;

import net.sevenspirals.saflute_template.pages.base.SAFluteTemplateArtifactPageBase;

import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.swt.widgets.Composite;

public class SAFluteBaseTemplateArtifactPage extends SAFluteTemplateArtifactPageBase {
    public SAFluteBaseTemplateArtifactPage(ProjectImportConfiguration projectImportConfiguration) {
        super(projectImportConfiguration);
    }

    @SuppressWarnings("restriction")
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        artifactComponent.setArtifactId("xxx-base");
        artifactComponent.setPackaging("pom");

        // Parentの入力項目を隠す(Baseが一番親になるので)
        parentComponent.setVisible(false);
    }
}
