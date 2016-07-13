package net.sevenspirals.saflute_template.pages.base;

import net.sevenspirals.saflute_template.wizards.base.AbstractSAFluteTemplateWizard;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class SAFluteTemplateProjectCreationPageBase extends WizardNewProjectCreationPage {

    protected Text eclipseProjectNameText;
    protected Text projectNameText;
    protected Text appNameText;
    protected SAFluteTemplateArtifactPageBase artifactPage;
    protected Label appNameLabel;

    public SAFluteTemplateProjectCreationPageBase(String pageName) {
        super(pageName);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        // TODO: 適切な初期値を設定する
        Composite control = (Composite) getControl();

        Control[] children = control.getChildren();
        Composite component1 = (Composite) children[0];
        Control[] children2 = component1.getChildren();
        eclipseProjectNameText = (Text) children2[1];

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        Composite composite = new Composite(control, SWT.NONE);
        composite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(2, false);
        composite.setLayout(gridLayout);

        Label label = new Label(composite, SWT.NONE);
        label.setText("SAFlute project name:");
        projectNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        projectNameText.setLayoutData(gridData);

        appNameLabel = new Label(composite, SWT.NONE);
        appNameLabel.setText("SAFlute app name:");
        appNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        appNameText.setLayoutData(gridData);
    }

    @SuppressWarnings("restriction")
    @Override
    public void setVisible(boolean visible) {
        AbstractSAFluteTemplateWizard wiz = (AbstractSAFluteTemplateWizard) getWizard();
        SAFluteTemplateArtifactPageBase page = (SAFluteTemplateArtifactPageBase) wiz.getPreviousPage(this);

        eclipseProjectNameText.setText(page.getArtifact().getArtifactId());

        String[] split = page.getArtifact().getArtifactId().split("-", 2);
        projectNameText.setText(StringUtils.capitalize(split[0]));
        if (split.length > 1)
            appNameText.setText(StringUtils.capitalize(split[1]));
        super.setVisible(visible);
    }

    public String getSAFluteProjectName() {
        return projectNameText.getText();
    }

    public String getAppName() {
        return appNameText.getText();
    }

}