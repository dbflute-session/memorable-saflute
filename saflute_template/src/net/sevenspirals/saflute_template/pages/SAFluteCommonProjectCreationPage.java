package net.sevenspirals.saflute_template.pages;

import net.sevenspirals.saflute_template.pages.base.SAFluteTemplateArtifactPageBase;
import net.sevenspirals.saflute_template.pages.base.SAFluteTemplateProjectCreationPageBase;

public class SAFluteCommonProjectCreationPage extends SAFluteTemplateProjectCreationPageBase {
    public SAFluteCommonProjectCreationPage(String pageName) {
        super(pageName);
    }

    public SAFluteCommonProjectCreationPage(String string, SAFluteTemplateArtifactPageBase artifactPage) {
        super(string);
        this.artifactPage = artifactPage;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        String text = this.appNameText.getText();
        if ("Common".equals(text))
            this.appNameText.setText("Yyy");
    }
}
