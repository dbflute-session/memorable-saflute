package net.sevenspirals.saflute_template.pages;

import net.sevenspirals.saflute_template.pages.base.SAFluteTemplateArtifactPageBase;

import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.swt.widgets.Composite;

public class SAFluteCommonTemplateArtifactPage extends SAFluteTemplateArtifactPageBase {

    public SAFluteCommonTemplateArtifactPage(ProjectImportConfiguration projectImportConfiguration) {
        super(projectImportConfiguration);
    }

    @SuppressWarnings("restriction")
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        artifactComponent.setArtifactId("xxx-common");
        artifactComponent.setPackaging("jar");
        parentComponent.getGroupIdCombo().setText("com.example.xxx");
        parentComponent.getArtifactIdCombo().setText("xxx-base");
        parentComponent.getVersionCombo().setText("1.0.0-SNAPSHOT");
        parentComponent.getVersionCombo().setEnabled(false);
    }
}
