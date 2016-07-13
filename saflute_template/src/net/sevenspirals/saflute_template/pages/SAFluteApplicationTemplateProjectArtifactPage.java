package net.sevenspirals.saflute_template.pages;

import net.sevenspirals.saflute_template.pages.base.SAFluteTemplateArtifactPageBase;

import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.swt.widgets.Composite;

public class SAFluteApplicationTemplateProjectArtifactPage extends SAFluteTemplateArtifactPageBase {

    public SAFluteApplicationTemplateProjectArtifactPage(ProjectImportConfiguration projectImportConfiguration) {
        super(projectImportConfiguration);
    }

    @SuppressWarnings("restriction")
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        artifactComponent.setPackaging("war");

        parentComponent.getGroupIdCombo().setText("com.example.xxx");
        parentComponent.getArtifactIdCombo().setText("xxx-base");

        parentComponent.getVersionCombo().setText("1.0.0-SNAPSHOT");
        parentComponent.getVersionCombo().setEnabled(false);

    }

}
