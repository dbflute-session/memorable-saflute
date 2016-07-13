package net.sevenspirals.saflute_template.pages;

import net.sevenspirals.saflute_template.pages.base.SAFluteTemplateArtifactPageBase;
import net.sevenspirals.saflute_template.pages.base.SAFluteTemplateProjectCreationPageBase;

import org.eclipse.swt.widgets.Composite;

public class SAFluteBaseProjectCreationPage extends SAFluteTemplateProjectCreationPageBase {
    public SAFluteBaseProjectCreationPage(String pageName) {
        super(pageName);
    }

    public SAFluteBaseProjectCreationPage(String string, SAFluteTemplateArtifactPageBase artifactPage) {
        super(string);
        this.artifactPage = artifactPage;
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        // おそらく使わないので固定値突っ込んで非表示
        this.appNameLabel.setVisible(false);
        this.appNameText.setText("Base");
        this.appNameText.setVisible(false);
    }
}
