package net.sevenspirals.saflute_template.pages.base;

import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.wizards.MavenArtifactComponent;
import org.eclipse.m2e.core.ui.internal.wizards.MavenParentComponent;
import org.eclipse.m2e.core.ui.internal.wizards.MavenProjectWizardArtifactPage;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class SAFluteTemplateArtifactPageBase extends MavenProjectWizardArtifactPage {

    public SAFluteTemplateArtifactPageBase(ProjectImportConfiguration projectImportConfiguration) {
        super(projectImportConfiguration);
    }

    public SAFluteTemplateArtifactPageBase(String name, ProjectImportConfiguration projectImportConfiguration) {
        super(name, projectImportConfiguration);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        // TODO: 適切な初期値を設定する

        artifactComponent.setGroupId("com.example.xxx");
        artifactComponent.setArtifactId("xxx-yyy");
        artifactComponent.setModelName("Xxx");

        // 消すとレイアウトが不自然なのでWizardPageを自作するまでVersionを固定
        artifactComponent.setVersion("1.0.0-SNAPSHOT");
        artifactComponent.getVersionCombo().setEnabled(false);
    }

    public MavenArtifactComponent getArtifact() {
        return artifactComponent;
    }

    public MavenParentComponent getParentArtifact() {
        return parentComponent;
    }

}