package net.sevenspirals.saflute_template.pages;

import net.sevenspirals.saflute_template.pages.base.SAFluteTemplateArtifactPageBase;
import net.sevenspirals.saflute_template.pages.base.SAFluteTemplateProjectCreationPageBase;


public class SAFluteApplicationProjectCreationPage extends SAFluteTemplateProjectCreationPageBase {

    public SAFluteApplicationProjectCreationPage(String pageName) {
        super(pageName);
    }

    public SAFluteApplicationProjectCreationPage(String string, SAFluteTemplateArtifactPageBase artifactPage) {
        super(string);
        this.artifactPage = artifactPage;
    }
}
