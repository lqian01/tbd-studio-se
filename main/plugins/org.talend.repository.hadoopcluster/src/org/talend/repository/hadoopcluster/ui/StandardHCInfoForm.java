// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.hadoopcluster.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.core.hadoop.repository.HadoopRepositoryUtil;
import org.talend.core.hadoop.version.EAuthenticationMode;
import org.talend.core.hadoop.version.EHadoopVersion4Drivers;
import org.talend.core.hadoop.version.custom.ECustomVersionGroup;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.hdfsbrowse.hadoop.service.EHadoopServiceType;
import org.talend.designer.hdfsbrowse.hadoop.service.HadoopServiceProperties;
import org.talend.designer.hdfsbrowse.hadoop.service.check.CheckHadoopServicesDialog;
import org.talend.designer.hdfsbrowse.manager.HadoopParameterValidator;
import org.talend.hadoop.distribution.DistributionFactory;
import org.talend.hadoop.distribution.component.MRComponent;
import org.talend.hadoop.distribution.helper.HadoopDistributionsHelper;
import org.talend.hadoop.distribution.model.DistributionBean;
import org.talend.hadoop.distribution.model.DistributionVersion;
import org.talend.metadata.managment.ui.dialog.HadoopPropertiesDialog;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.ui.utils.ExtendedNodeConnectionContextUtils.EHadoopParamName;
import org.talend.repository.hadoopcluster.conf.HadoopConfsUtils;
import org.talend.repository.hadoopcluster.i18n.Messages;
import org.talend.repository.hadoopcluster.ui.common.AbstractHadoopForm;
import org.talend.repository.hadoopcluster.ui.common.IHadoopClusterInfoForm;
import org.talend.repository.hadoopcluster.util.HCRepositoryUtil;
import org.talend.repository.hadoopcluster.util.HCVersionUtil;
import org.talend.repository.model.hadoopcluster.HadoopClusterConnection;
import org.talend.repository.model.hadoopcluster.HadoopClusterConnectionItem;

/**
 * 
 * created by ycbai on 2014年9月16日 Detailled comment
 *
 */
public class StandardHCInfoForm extends AbstractHadoopForm<HadoopClusterConnection> implements IHadoopClusterInfoForm {

    private Composite parentForm;

    private Composite hadoopPropertiesComposite;

    private LabelledCombo authenticationCombo;

    private Button kerberosBtn;

    private Button keytabBtn;

    private Button useClouderaNaviBtn;

    private Button clouderaNaviButton;

    private LabelledText namenodeUriText;

    private LabelledText jobtrackerUriText;

    private LabelledText rmSchedulerText;

    private LabelledText jobHistoryText;

    private LabelledText stagingDirectoryText;

    private LabelledText namenodePrincipalText;

    private LabelledText jtOrRmPrincipalText;

    private LabelledText jobHistoryPrincipalText;

    private LabelledText keytabPrincipalText;

    private LabelledText userNameText;

    private LabelledText groupText;

    private Button useDNHostBtn;

    private Button hadoopConfsButton;

    private Button useCustomConfBtn;

    private LabelledFileField keytabText;

    private Group customGroup;

    private HadoopPropertiesDialog propertiesDialog;

    private UtilsButton checkServicesBtn;

    private final boolean creation;

    protected DistributionBean hadoopDistribution;

    protected DistributionVersion hadoopVersison;

    private boolean needInitializeContext = false;

    // Mapr Ticket Authentication
    private Button maprTBtn;

    private LabelledText maprTPasswordText;

    private LabelledText maprTClusterText;

    private LabelledText maprTDurationText;

    private Button setMaprTHomeDirBtn;

    private Button setHadoopLoginBtn;

    private Button preloadAuthentificationBtn;

    private LabelledText maprTHomeDirText;

    private LabelledText maprTHadoopLoginText;

    private Composite authMaprTComposite;

    private Composite maprTPCDCompposite;

    private Composite maprTSetComposite;

    public StandardHCInfoForm(Composite parent, ConnectionItem connectionItem, String[] existingNames, boolean creation,
            DistributionBean hadoopDistribution, DistributionVersion hadoopVersison) {
        super(parent, SWT.NONE, existingNames);
        this.parentForm = parent;
        this.connectionItem = connectionItem;
        this.creation = creation;
        this.hadoopDistribution = hadoopDistribution;
        this.hadoopVersison = hadoopVersison;
        setConnectionItem(connectionItem);
        setupForm(true);
        init();
        setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = (GridLayout) getLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout(layout);
    }

    @Override
    public void initialize() {
        // initialize for context mode
        if (needInitializeContext) {
            init();
        }
    }

    @Override
    public void init() {
        if (!isContextMode()) {
            fillDefaults();
        }

        EAuthenticationMode authMode = EAuthenticationMode.getAuthenticationByName(getConnection().getAuthMode(), false);
        if (authMode != null) {
            authenticationCombo.setText(authMode.getDisplayName());
        } else {
            authenticationCombo.select(0);
        }
        HadoopClusterConnection connection = getConnection();
        namenodeUriText.setText(connection.getNameNodeURI());
        jobtrackerUriText.setText(connection.getJobTrackerURI());
        rmSchedulerText.setText(StringUtils.trimToEmpty(connection.getRmScheduler()));
        jobHistoryText.setText(StringUtils.trimToEmpty(connection.getJobHistory()));
        stagingDirectoryText.setText(StringUtils.trimToEmpty(connection.getStagingDirectory()));
        useDNHostBtn.setSelection(connection.isUseDNHost());
        useCustomConfBtn.setSelection(connection.isUseCustomConfs());
        if (useClouderaNaviBtn != null) {
            useClouderaNaviBtn.setSelection(connection.isUseClouderaNavi());
        }
        kerberosBtn.setSelection(connection.isEnableKerberos());
        namenodePrincipalText.setText(connection.getPrincipal());
        jtOrRmPrincipalText.setText(connection.getJtOrRmPrincipal());
        jobHistoryPrincipalText.setText(connection.getJobHistoryPrincipal());
        keytabBtn.setSelection(connection.isUseKeytab());
        keytabPrincipalText.setText(connection.getKeytabPrincipal());
        keytabText.setText(connection.getKeytab());
        userNameText.setText(connection.getUserName());
        groupText.setText(connection.getGroup());

        //
        maprTBtn.setSelection(connection.isEnableMaprT());
        maprTPasswordText.setText(connection.getMaprTPassword());
        maprTClusterText.setText(connection.getMaprTCluster());
        maprTDurationText.setText(connection.getMaprTDuration());
        setMaprTHomeDirBtn.setSelection(connection.isSetMaprTHomeDir());
        setHadoopLoginBtn.setSelection(connection.isSetHadoopLogin());
        preloadAuthentificationBtn.setSelection(connection.isPreloadAuthentification());
        maprTHomeDirText.setText(connection.getMaprTHomeDir());
        maprTHadoopLoginText.setText(connection.getMaprTHadoopLogin());

        needInitializeContext = true;
        updateStatus(IStatus.OK, EMPTY_STRING);
    }

    @Override
    protected void adaptFormToReadOnly() {
        readOnly = isReadOnly();
        namenodeUriText.setReadOnly(readOnly);
        jobtrackerUriText.setReadOnly(readOnly);
        rmSchedulerText.setReadOnly(readOnly);
        jobHistoryText.setReadOnly(readOnly);
        stagingDirectoryText.setReadOnly(readOnly);
        useDNHostBtn.setEnabled(!readOnly);
        useCustomConfBtn.setEnabled(!readOnly);
        hadoopConfsButton.setEnabled(!readOnly && useCustomConfBtn.getSelection());
        if (useClouderaNaviBtn != null) {
            useClouderaNaviBtn.setEnabled(!readOnly);
            clouderaNaviButton.setEnabled(!readOnly && useClouderaNaviBtn.getSelection());
        }
        kerberosBtn.setEnabled(!readOnly);
        namenodePrincipalText.setReadOnly(readOnly);
        jtOrRmPrincipalText.setReadOnly(readOnly);
        jobHistoryPrincipalText.setReadOnly(readOnly);
        userNameText.setReadOnly(readOnly);
        groupText.setReadOnly(readOnly);

        kerberosBtn.setEnabled(!readOnly);
        namenodePrincipalText.setReadOnly(readOnly);
        jtOrRmPrincipalText.setReadOnly(readOnly);
        jobHistoryPrincipalText.setReadOnly(readOnly);
        userNameText.setReadOnly(readOnly);
        groupText.setReadOnly(readOnly);

        maprTBtn.setEnabled(!readOnly);
        maprTPasswordText.setReadOnly(readOnly);
        maprTClusterText.setReadOnly(readOnly);
        maprTDurationText.setReadOnly(readOnly);
        setMaprTHomeDirBtn.setEnabled(!readOnly);
        setHadoopLoginBtn.setEnabled(!readOnly);
        preloadAuthentificationBtn.setEnabled(!readOnly);
        maprTHomeDirText.setReadOnly(readOnly);
        maprTHadoopLoginText.setReadOnly(readOnly);
    }

    @Override
    protected void updateEditableStatus(boolean isEditable) {
        authenticationCombo.setEnabled(isEditable);
        namenodeUriText.setEditable(isEditable);
        jobtrackerUriText.setEditable(isEditable);
        rmSchedulerText.setEditable(isEditable);
        jobHistoryText.setEditable(isEditable);
        stagingDirectoryText.setEditable(isEditable);
        useDNHostBtn.setEnabled(isEditable);
        kerberosBtn.setEnabled(isEditable && (isCurrentHadoopVersionSupportSecurity() || isCustomUnsupportHasSecurity()));
        boolean isKerberosEditable = kerberosBtn.isEnabled() && kerberosBtn.getSelection();
        namenodePrincipalText.setEditable(isKerberosEditable);
        jtOrRmPrincipalText.setEditable(isKerberosEditable);
        jobHistoryPrincipalText.setEditable(isKerberosEditable);
        userNameText.setEditable(isEditable && !kerberosBtn.getSelection());
        groupText.setEditable(isEditable && (isCurrentHadoopVersionSupportGroup() || isCustomUnsupportHasGroup()));
        keytabBtn.setEnabled(isEditable && kerberosBtn.getSelection());
        boolean isKeyTabEditable = keytabBtn.isEnabled() && keytabBtn.getSelection();
        keytabText.setEditable(isKeyTabEditable);
        keytabPrincipalText.setEditable(isKeyTabEditable);

        //
        maprTBtn.setEnabled(isEditable && isCurrentHadoopVersionSupportMapRTicket());
        boolean isMaprTEditable = maprTBtn.isEnabled() && maprTBtn.getSelection();
        maprTPasswordText.setEditable(isMaprTEditable && !isKerberosEditable);
        maprTClusterText.setEditable(isMaprTEditable);
        maprTDurationText.setEditable(isMaprTEditable);
        setMaprTHomeDirBtn.setEnabled(isEditable && maprTBtn.getSelection());
        setHadoopLoginBtn.setEnabled(isEditable && maprTBtn.getSelection());
        preloadAuthentificationBtn.setEnabled(isEditable && maprTBtn.getSelection());
        maprTHomeDirText.setEditable(isMaprTEditable);
        maprTHadoopLoginText.setEditable(isMaprTEditable);

        hadoopPropertiesComposite.setEnabled(isEditable);
        propertiesDialog.updateStatusLabel(getHadoopProperties());
    }

    @Override
    protected void addFields() {
        addCustomFields();
        addConnectionFields();
        addAuthenticationFields();
        addHadoopPropertiesFields();
        addNavigatorFields();
        addHadoopConfsFields();

        addCheckFields();
    }

    private void addCustomFields() {
        customGroup = Form.createGroup(this, 4, Messages.getString("HadoopClusterForm.customSettings")); //$NON-NLS-1$
        customGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        authenticationCombo = new LabelledCombo(customGroup,
                Messages.getString("HadoopClusterForm.authentication"), //$NON-NLS-1$
                Messages.getString("HadoopClusterForm.authentication.tooltip"), EAuthenticationMode.getAllAuthenticationDisplayNames() //$NON-NLS-1$
                        .toArray(new String[0]), 1, false);
    }

    private void addConnectionFields() {
        Group connectionGroup = Form.createGroup(this, 1, Messages.getString("HadoopClusterForm.connectionSettings"), 110); //$NON-NLS-1$
        connectionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Composite uriPartComposite = new Composite(connectionGroup, SWT.NULL);
        GridLayout uriPartLayout = new GridLayout(2, false);
        uriPartLayout.marginWidth = 0;
        uriPartLayout.marginHeight = 0;
        uriPartComposite.setLayout(uriPartLayout);
        uriPartComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        namenodeUriText = new LabelledText(uriPartComposite, Messages.getString("HadoopClusterForm.text.namenodeURI"), 1); //$NON-NLS-1$
        jobtrackerUriText = new LabelledText(uriPartComposite, Messages.getString("HadoopClusterForm.text.jobtrackerURI"), 1); //$NON-NLS-1$
        rmSchedulerText = new LabelledText(uriPartComposite, Messages.getString("HadoopClusterForm.text.rmScheduler"), 1); //$NON-NLS-1$
        jobHistoryText = new LabelledText(uriPartComposite, Messages.getString("HadoopClusterForm.text.jobHistory"), 1); //$NON-NLS-1$
        stagingDirectoryText = new LabelledText(uriPartComposite,
                Messages.getString("HadoopClusterForm.text.stagingDirectory"), 1); //$NON-NLS-1$
        useDNHostBtn = new Button(uriPartComposite, SWT.CHECK);
        useDNHostBtn.setText(Messages.getString("HadoopClusterForm.button.useDNHost")); //$NON-NLS-1$
        useDNHostBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
    }

    private void addAuthenticationFields() {
        Group authGroup = Form.createGroup(this, 1, Messages.getString("HadoopClusterForm.authenticationSettings"), 110); //$NON-NLS-1$
        authGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite authPartComposite = new Composite(authGroup, SWT.NULL);
        GridLayout authPartLayout = new GridLayout(1, false);
        authPartLayout.marginWidth = 0;
        authPartLayout.marginHeight = 0;
        authPartComposite.setLayout(authPartLayout);
        authPartComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite authCommonComposite = new Composite(authPartComposite, SWT.NULL);
        GridLayout authCommonCompLayout = new GridLayout(4, false);
        authCommonCompLayout.marginWidth = 0;
        authCommonCompLayout.marginHeight = 0;
        authCommonComposite.setLayout(authCommonCompLayout);
        authCommonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        kerberosBtn = new Button(authCommonComposite, SWT.CHECK);
        kerberosBtn.setText(Messages.getString("HadoopClusterForm.button.kerberos")); //$NON-NLS-1$
        kerberosBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
        namenodePrincipalText = new LabelledText(authCommonComposite,
                Messages.getString("HadoopClusterForm.text.namenodePrincipal"), 1); //$NON-NLS-1$
        jtOrRmPrincipalText = new LabelledText(authCommonComposite, Messages.getString("HadoopClusterForm.text.rmPrincipal"), 1); //$NON-NLS-1$
        jobHistoryPrincipalText = new LabelledText(authCommonComposite,
                Messages.getString("HadoopClusterForm.text.jobHistoryPrincipal"), 1); //$NON-NLS-1$

        // placeHolder is only used to make userNameText and groupText to new line
        Composite placeHolder = new Composite(authCommonComposite, SWT.NULL);
        GridData placeHolderLayoutData = new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1);
        placeHolderLayoutData.heightHint = 1;
        placeHolder.setLayoutData(placeHolderLayoutData);

        userNameText = new LabelledText(authCommonComposite, Messages.getString("HadoopClusterForm.text.userName"), 1); //$NON-NLS-1$
        groupText = new LabelledText(authCommonComposite, Messages.getString("HadoopClusterForm.text.group"), 1); //$NON-NLS-1$

        Composite authKeytabComposite = new Composite(authGroup, SWT.NULL);
        GridLayout authKeytabCompLayout = new GridLayout(5, false);
        authKeytabCompLayout.marginWidth = 0;
        authKeytabCompLayout.marginHeight = 0;
        authKeytabComposite.setLayout(authKeytabCompLayout);
        authKeytabComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        keytabBtn = new Button(authKeytabComposite, SWT.CHECK);
        keytabBtn.setText(Messages.getString("HadoopClusterForm.button.keytab")); //$NON-NLS-1$
        keytabBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 5, 1));
        keytabPrincipalText = new LabelledText(authKeytabComposite,
                Messages.getString("HadoopClusterForm.text.keytabPrincipal"), 1); //$NON-NLS-1$
        String[] extensions = { "*.*" }; //$NON-NLS-1$
        keytabText = new LabelledFileField(authKeytabComposite, Messages.getString("HadoopClusterForm.text.keytab"), extensions); //$NON-NLS-1$

        // Mapr Ticket Authentication
        authMaprTComposite = new Composite(authGroup, SWT.NULL);
        GridLayout authMaprTicketCompLayout = new GridLayout(1, false);
        authMaprTicketCompLayout.marginWidth = 0;
        authMaprTicketCompLayout.marginHeight = 0;
        authMaprTComposite.setLayout(authMaprTicketCompLayout);
        authMaprTComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        maprTBtn = new Button(authMaprTComposite, SWT.CHECK);
        maprTBtn.setText(Messages.getString("HadoopClusterForm.button.maprTicket")); //$NON-NLS-1$
        maprTBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

        maprTPCDCompposite = new Composite(authMaprTComposite, SWT.NULL);
        GridLayout maprTPCDCompositeLayout = new GridLayout(2, false);
        maprTPCDCompositeLayout.marginWidth = 0;
        maprTPCDCompositeLayout.marginHeight = 0;
        maprTPCDCompposite.setLayout(maprTPCDCompositeLayout);
        maprTPCDCompposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        maprTPasswordText = new LabelledText(maprTPCDCompposite,
                Messages.getString("HadoopClusterForm.text.maprTPassword"), 1, SWT.PASSWORD); //$NON-NLS-1$
        maprTClusterText = new LabelledText(maprTPCDCompposite, Messages.getString("HadoopClusterForm.text.maprTCluster"), 1); //$NON-NLS-1$
        maprTDurationText = new LabelledText(maprTPCDCompposite, Messages.getString("HadoopClusterForm.text.maprTDuration"), 1); //$NON-NLS-1$

        maprTSetComposite = new Composite(authMaprTComposite, SWT.NULL);
        GridLayout maprTicketSetCompLayout = new GridLayout(3, false);
        maprTicketSetCompLayout.marginWidth = 0;
        maprTicketSetCompLayout.marginHeight = 0;
        maprTSetComposite.setLayout(maprTicketSetCompLayout);
        maprTSetComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        setMaprTHomeDirBtn = new Button(maprTSetComposite, SWT.CHECK);
        setMaprTHomeDirBtn.setText(Messages.getString("HadoopClusterForm.button.setMaprTHomeDir")); //$NON-NLS-1$
        maprTHomeDirText = new LabelledText(maprTSetComposite, "", 1); //$NON-NLS-1$

        setHadoopLoginBtn = new Button(maprTSetComposite, SWT.CHECK);
        setHadoopLoginBtn.setText(Messages.getString("HadoopClusterForm.button.setHadoopLogin")); //$NON-NLS-1$
        maprTHadoopLoginText = new LabelledText(maprTSetComposite, "", 1); //$NON-NLS-1$

        preloadAuthentificationBtn = new Button(maprTSetComposite, SWT.CHECK);
        preloadAuthentificationBtn.setText(Messages.getString("HadoopClusterForm.button.preloadAuthentification")); //$NON-NLS-1$
    }

    private void addHadoopPropertiesFields() {
        hadoopPropertiesComposite = new Composite(this, SWT.NONE);
        GridLayout hadoopPropertiesLayout = new GridLayout(1, false);
        hadoopPropertiesLayout.marginWidth = 0;
        hadoopPropertiesLayout.marginHeight = 0;
        hadoopPropertiesComposite.setLayout(hadoopPropertiesLayout);
        hadoopPropertiesComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        List<Map<String, Object>> hadoopPropertiesList = getHadoopProperties();
        propertiesDialog = new HadoopPropertiesDialog(getShell(), hadoopPropertiesList) {

            @Override
            public void applyProperties(List<Map<String, Object>> properties) {
                getConnection().setHadoopProperties(HadoopRepositoryUtil.getHadoopPropertiesJsonStr(properties));
            }

        };
        propertiesDialog.createPropertiesFields(hadoopPropertiesComposite);
    }

    private List<Map<String, Object>> getHadoopProperties() {
        String hadoopProperties = getConnection().getHadoopProperties();
        List<Map<String, Object>> hadoopPropertiesList = HadoopRepositoryUtil.getHadoopPropertiesList(hadoopProperties);
        return hadoopPropertiesList;
    }

    private void addNavigatorFields() {
        DistributionBean distriBean = getDistribution();
        MRComponent currentDistribution;
        boolean isShow = false;
        try {
            currentDistribution = (MRComponent) DistributionFactory.buildDistribution(distriBean.getName(),
                    hadoopVersison.getVersion());
            isShow = !getDistribution().useCustom() && currentDistribution.doSupportClouderaNavigator();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        if (!isShow) {
            return;
        }

        Composite clouderaNaviComposite = new Composite(this, SWT.NONE);
        GridLayout hadoopConfsCompLayout = new GridLayout(3, false);
        hadoopConfsCompLayout.marginWidth = 5;
        hadoopConfsCompLayout.marginHeight = 5;
        clouderaNaviComposite.setLayout(hadoopConfsCompLayout);
        clouderaNaviComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        useClouderaNaviBtn = new Button(clouderaNaviComposite, SWT.CHECK);
        useClouderaNaviBtn.setText(Messages.getString("HadoopClusterForm.button.use_cloudera_navigator")); //$NON-NLS-1$
        useClouderaNaviBtn.setLayoutData(new GridData());

        clouderaNaviButton = new Button(clouderaNaviComposite, SWT.NONE);
        clouderaNaviButton.setImage(ImageProvider.getImage(EImage.THREE_DOTS_ICON));
        clouderaNaviButton.setLayoutData(new GridData(30, 25));
        clouderaNaviButton.setEnabled(false);

        Label displayLabel = new Label(clouderaNaviComposite, SWT.NONE);
        displayLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    }

    private void addHadoopConfsFields() {
        Composite hadoopConfsComposite = new Composite(this, SWT.NONE);
        GridLayout hadoopConfsCompLayout = new GridLayout(3, false);
        hadoopConfsCompLayout.marginWidth = 5;
        hadoopConfsCompLayout.marginHeight = 5;
        hadoopConfsComposite.setLayout(hadoopConfsCompLayout);
        hadoopConfsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        useCustomConfBtn = new Button(hadoopConfsComposite, SWT.CHECK);
        useCustomConfBtn.setText(Messages.getString("HadoopClusterForm.button.useCustomConf")); //$NON-NLS-1$
        useCustomConfBtn.setLayoutData(new GridData());

        hadoopConfsButton = new Button(hadoopConfsComposite, SWT.NONE);
        hadoopConfsButton.setImage(ImageProvider.getImage(EImage.THREE_DOTS_ICON));
        hadoopConfsButton.setLayoutData(new GridData(30, 25));
        hadoopConfsButton.setEnabled(false);

        Label displayLabel = new Label(hadoopConfsComposite, SWT.NONE);
        displayLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void addCheckFields() {
        Composite checkGroup = new Composite(this, SWT.NONE);
        GridLayout checkGridLayout = new GridLayout(1, false);
        checkGroup.setLayout(checkGridLayout);
        GridData checkGridData = new GridData(GridData.FILL_HORIZONTAL);
        checkGridData.minimumHeight = 5;
        checkGroup.setLayoutData(checkGridData);
        Composite checkButtonComposite = Form.startNewGridLayout(checkGroup, 1, false, SWT.CENTER, SWT.BOTTOM);
        GridLayout checkButtonLayout = (GridLayout) checkButtonComposite.getLayout();
        checkButtonLayout.marginHeight = 0;
        checkButtonLayout.marginWidth = 0;
        checkServicesBtn = new UtilsButton(checkButtonComposite, Messages.getString("HadoopClusterForm.button.check"), true); //$NON-NLS-1$
        checkServicesBtn.setEnabled(false);
    }

    @Override
    protected void addFieldsListeners() {
        authenticationCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                String newAuthDisplayName = authenticationCombo.getText();
                EAuthenticationMode newAuthMode = EAuthenticationMode.getAuthenticationByDisplayName(newAuthDisplayName);
                String originalAuthName = getConnection().getAuthMode();
                EAuthenticationMode originalAuthMode = EAuthenticationMode.getAuthenticationByName(originalAuthName, false);
                if (newAuthMode != null && newAuthMode != originalAuthMode) {
                    getConnection().setAuthMode(newAuthMode.getName());
                    updateForm();
                    checkFieldsValue();
                }
            }
        });

        namenodeUriText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setNameNodeURI(namenodeUriText.getText());
                checkFieldsValue();
            }
        });

        jobtrackerUriText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setJobTrackerURI(jobtrackerUriText.getText());
                checkFieldsValue();
            }
        });

        rmSchedulerText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setRmScheduler(rmSchedulerText.getText());
                checkFieldsValue();
            }
        });

        jobHistoryText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setJobHistory(jobHistoryText.getText());
                checkFieldsValue();
            }
        });

        stagingDirectoryText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setStagingDirectory(stagingDirectoryText.getText());
                checkFieldsValue();
            }
        });

        useDNHostBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getConnection().setUseDNHost(useDNHostBtn.getSelection());
                checkFieldsValue();
            }
        });

        useCustomConfBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                hadoopConfsButton.setEnabled(useCustomConfBtn.getSelection());
                getConnection().setUseCustomConfs(useCustomConfBtn.getSelection());
                checkFieldsValue();
            }
        });

        hadoopConfsButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                AbstractHadoopForm form = null;
                if (parentForm instanceof AbstractHadoopForm) {
                    form = (AbstractHadoopForm) parentForm;
                }
                HadoopConfsUtils.openHadoopConfsWizard(form, (HadoopClusterConnectionItem) connectionItem, creation);
            }
        });
        if (useClouderaNaviBtn != null) {
            useClouderaNaviBtn.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    clouderaNaviButton.setEnabled(useClouderaNaviBtn.getSelection());
                    getConnection().setUseClouderaNavi(useClouderaNaviBtn.getSelection());
                    checkFieldsValue();
                }
            });
            clouderaNaviButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    AbstractHadoopForm form = null;
                    if (parentForm instanceof AbstractHadoopForm) {
                        form = (AbstractHadoopForm) parentForm;
                    }
                    HadoopConfsUtils.openClouderaNaviWizard(form, (HadoopClusterConnectionItem) connectionItem, creation);
                }
            });
        }

        rmSchedulerText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setRmScheduler(rmSchedulerText.getText());
                checkFieldsValue();
            }
        });

        jobHistoryText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setJobHistory(jobHistoryText.getText());
                checkFieldsValue();
            }
        });

        stagingDirectoryText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setStagingDirectory(stagingDirectoryText.getText());
                checkFieldsValue();
            }
        });

        useDNHostBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getConnection().setUseDNHost(useDNHostBtn.getSelection());
                checkFieldsValue();
            }
        });

        useCustomConfBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                hadoopConfsButton.setEnabled(useCustomConfBtn.getSelection());
                getConnection().setUseCustomConfs(useCustomConfBtn.getSelection());
                checkFieldsValue();
            }
        });

        namenodePrincipalText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setPrincipal(namenodePrincipalText.getText());
                checkFieldsValue();
            }
        });

        jtOrRmPrincipalText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setJtOrRmPrincipal(jtOrRmPrincipalText.getText());
                checkFieldsValue();
            }
        });

        jobHistoryPrincipalText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                getConnection().setJobHistoryPrincipal(jobHistoryPrincipalText.getText());
                checkFieldsValue();
            }
        });

        userNameText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setUserName(userNameText.getText());
                checkFieldsValue();
            }
        });

        groupText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setGroup(groupText.getText());
                checkFieldsValue();
            }
        });

        kerberosBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getConnection().setEnableKerberos(kerberosBtn.getSelection());
                updateForm();
                checkFieldsValue();
            }
        });

        keytabBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getConnection().setUseKeytab(keytabBtn.getSelection());
                updateForm();
                checkFieldsValue();
            }
        });

        keytabPrincipalText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setKeytabPrincipal(keytabPrincipalText.getText());
                checkFieldsValue();
            }
        });

        keytabText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setKeytab(keytabText.getText());
                checkFieldsValue();
            }
        });

        maprTBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (maprTBtn.getSelection()) {
                    hideControl(maprTPCDCompposite, false);
                    hideControl(maprTSetComposite, false);
                } else {
                    hideControl(maprTPCDCompposite, true);
                    hideControl(maprTSetComposite, true);
                }
                authMaprTComposite.layout();
                authMaprTComposite.getParent().layout();

                getConnection().setEnableMaprT(maprTBtn.getSelection());
                updateForm();
                checkFieldsValue();
            }
        });
        maprTPasswordText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setMaprTPassword(maprTPasswordText.getText());
                checkFieldsValue();
            }
        });
        maprTClusterText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setMaprTCluster(maprTClusterText.getText());
                checkFieldsValue();
            }
        });
        maprTDurationText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setMaprTDuration(maprTDurationText.getText());
                checkFieldsValue();
            }
        });
        setMaprTHomeDirBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getConnection().setSetMaprTHomeDir(setMaprTHomeDirBtn.getSelection());
                updateForm();
                checkFieldsValue();
            }
        });
        maprTHomeDirText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setMaprTHomeDir(maprTHomeDirText.getText());
                checkFieldsValue();
            }
        });
        setHadoopLoginBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getConnection().setSetHadoopLogin(setHadoopLoginBtn.getSelection());
                updateForm();
                checkFieldsValue();
            }
        });
        maprTHadoopLoginText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                getConnection().setMaprTHadoopLogin(maprTHadoopLoginText.getText());
                checkFieldsValue();
            }
        });
        preloadAuthentificationBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getConnection().setPreloadAuthentification(preloadAuthentificationBtn.getSelection());
                updateForm();
                checkFieldsValue();
            }
        });

    }

    @Override
    protected void addUtilsButtonListeners() {
        checkServicesBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                checkServices();
            }
        });
    }

    private void checkServices() {
        Map<EHadoopServiceType, HadoopServiceProperties> serviceTypeToProperties = new HashMap<EHadoopServiceType, HadoopServiceProperties>();
        HadoopServiceProperties nnProperties = new HadoopServiceProperties();
        initCommonProperties(nnProperties);
        nnProperties.setNameNode(getConnection().getNameNodeURI());
        serviceTypeToProperties.put(EHadoopServiceType.NAMENODE, nnProperties);
        HadoopServiceProperties rmORjtProperties = new HadoopServiceProperties();
        initCommonProperties(rmORjtProperties);
        if (getConnection().isUseYarn()) {
            rmORjtProperties.setResourceManager(getConnection().getJobTrackerURI());
            serviceTypeToProperties.put(EHadoopServiceType.RESOURCE_MANAGER, rmORjtProperties);
        } else {
            rmORjtProperties.setJobTracker(getConnection().getJobTrackerURI());
            serviceTypeToProperties.put(EHadoopServiceType.JOBTRACKER, rmORjtProperties);
        }
        if (getConnection().isUseCustomVersion()) {
            nnProperties.setUid(connectionItem.getProperty().getId() + ":" + ECustomVersionGroup.COMMON.getName()); //$NON-NLS-1$
            nnProperties.setCustomJars(HCVersionUtil.getCustomVersionMap(getConnection()).get(
                    ECustomVersionGroup.COMMON.getName()));
            rmORjtProperties.setUid(connectionItem.getProperty().getId() + ":" + ECustomVersionGroup.MAP_REDUCE.getName()); //$NON-NLS-1$
            rmORjtProperties.setCustomJars(HCVersionUtil.getCustomVersionMap(getConnection()).get(
                    ECustomVersionGroup.MAP_REDUCE.getName()));
        }
        new CheckHadoopServicesDialog(getShell(), serviceTypeToProperties).open();
    }

    private void initCommonProperties(HadoopServiceProperties properties) {
        HadoopClusterConnection connection = getConnection();
        ContextType contextType = null;
        if (getConnection().isContextMode()) {
            contextType = ConnectionContextHelper.getContextTypeForContextMode(connection);
        }
        properties.setContextType(contextType);
        properties.setDistribution(connection.getDistribution());
        properties.setVersion(connection.getDfVersion());
        properties.setGroup(connection.getGroup());
        properties.setUseKrb(connection.isEnableKerberos());
        properties.setCustom(connection.isUseCustomVersion());
        properties.setUseCustomConfs(connection.isUseCustomConfs());
        properties.setPrincipal(connection.getPrincipal());
        properties.setJtOrRmPrincipal(connection.getJtOrRmPrincipal());
        properties.setJobHistoryPrincipal(connection.getJobHistoryPrincipal());
        properties.setUseKeytab(connection.isUseKeytab());
        properties.setKeytabPrincipal(connection.getKeytabPrincipal());
        properties.setKeytab(connection.getKeytab());
        properties.setHadoopProperties(HadoopRepositoryUtil.getHadoopPropertiesList(connection.getHadoopProperties()));
        properties.setRelativeHadoopClusterId(connectionItem.getProperty().getId());
    }

    @Override
    public void updateForm() {
        HadoopClusterConnection connection = getConnection();
        final DistributionVersion distributionVersion = getDistributionVersion();
        if (distributionVersion.distribution.useCustom()) {
            hideControl(customGroup, false);
            String authModeName = connection.getAuthMode();
            if (authModeName != null) {
                EAuthenticationMode authMode = EAuthenticationMode.getAuthenticationByName(authModeName, false);
                switch (authMode) {
                case KRB:
                    kerberosBtn.setEnabled(true);
                    namenodePrincipalText.setEditable(kerberosBtn.isEnabled() && kerberosBtn.getSelection());
                    jtOrRmPrincipalText.setEditable(namenodePrincipalText.getEditable());
                    jobHistoryPrincipalText.setEditable(namenodePrincipalText.getEditable());
                    keytabBtn.setEnabled(kerberosBtn.isEnabled() && kerberosBtn.getSelection());
                    keytabPrincipalText.setEditable(keytabBtn.isEnabled() && keytabBtn.getSelection());
                    keytabText.setEditable(keytabBtn.isEnabled() && keytabBtn.getSelection());
                    userNameText.setEditable(false);
                    groupText.setEditable(false);
                    break;
                case UGI:
                    kerberosBtn.setEnabled(false);
                    namenodePrincipalText.setEditable(false);
                    jtOrRmPrincipalText.setEditable(false);
                    jobHistoryPrincipalText.setEditable(false);
                    keytabBtn.setEnabled(false);
                    keytabPrincipalText.setEditable(false);
                    keytabText.setEditable(false);
                    userNameText.setEditable(true);
                    groupText.setEditable(true);
                    break;
                default:
                    kerberosBtn.setEnabled(false);
                    namenodePrincipalText.setEditable(false);
                    jtOrRmPrincipalText.setEditable(false);
                    jobHistoryPrincipalText.setEditable(false);
                    keytabBtn.setEnabled(false);
                    keytabPrincipalText.setEditable(false);
                    keytabText.setEditable(false);
                    userNameText.setEditable(true);
                    groupText.setEditable(false);

                    // maprt
                    maprTBtn.setEnabled(false);
                    maprTPasswordText.setEditable(false);
                    maprTClusterText.setEditable(false);
                    maprTDurationText.setEditable(false);
                    setMaprTHomeDirBtn.setEnabled(false);
                    setHadoopLoginBtn.setEnabled(false);
                    preloadAuthentificationBtn.setEnabled(false);
                    maprTHomeDirText.setEditable(false);
                    maprTHadoopLoginText.setEditable(false);
                    break;
                }
            }
        } else {
            hideControl(customGroup, true);

            kerberosBtn.setEnabled(isCurrentHadoopVersionSupportSecurity());
            namenodePrincipalText.setEditable(kerberosBtn.isEnabled() && kerberosBtn.getSelection());
            jtOrRmPrincipalText.setEditable(namenodePrincipalText.getEditable());
            jobHistoryPrincipalText.setEditable(namenodePrincipalText.getEditable()
                    && isCurrentHadoopVersionSupportJobHistoryPrincipal());
            keytabBtn.setEnabled(kerberosBtn.isEnabled() && kerberosBtn.getSelection());
            keytabPrincipalText.setEditable(keytabBtn.isEnabled() && keytabBtn.getSelection());
            keytabText.setEditable(keytabBtn.isEnabled() && keytabBtn.getSelection());
            groupText.setEditable(isCurrentHadoopVersionSupportGroup());
            userNameText.setEditable(!kerberosBtn.getSelection());

            // maprt
            maprTBtn.setEnabled(isCurrentHadoopVersionSupportMapRTicket());
            maprTPasswordText.setEditable(maprTBtn.isEnabled()
                    && (maprTBtn.getSelection() && !(kerberosBtn.isEnabled() && kerberosBtn.getSelection())));
            maprTClusterText.setEditable(maprTBtn.isEnabled() && maprTBtn.getSelection());
            maprTDurationText.setEditable(maprTBtn.isEnabled() && maprTBtn.getSelection());
            setMaprTHomeDirBtn.setEnabled(maprTBtn.isEnabled() && maprTBtn.getSelection());
            setHadoopLoginBtn.setEnabled(maprTBtn.isEnabled() && maprTBtn.getSelection());
            preloadAuthentificationBtn.setEnabled(maprTBtn.isEnabled() && maprTBtn.getSelection());
            maprTHomeDirText.setEditable(maprTBtn.isEnabled() && maprTBtn.getSelection() && setMaprTHomeDirBtn.isEnabled()
                    && setMaprTHomeDirBtn.getSelection());
            maprTHadoopLoginText.setEditable(maprTBtn.isEnabled() && maprTBtn.getSelection() && setHadoopLoginBtn.isEnabled()
                    && setHadoopLoginBtn.getSelection());

            if (maprTBtn.getSelection()) {
                hideControl(maprTPCDCompposite, false);
                hideControl(maprTSetComposite, false);
            } else {
                hideControl(maprTPCDCompposite, true);
                hideControl(maprTSetComposite, true);
            }
            authMaprTComposite.layout();
            authMaprTComposite.getParent().layout();

        }
        updateMRRelatedContent();
        updateConnectionContent();
        if (isContextMode()) {
            adaptFormToEditable();
        }
    }

    /**
     * is current hadoop version support JobHistoryPrincipal
     * 
     * @return
     */
    protected boolean isCurrentHadoopVersionSupportJobHistoryPrincipal() {
        if (hadoopDistribution == null || hadoopVersison == null) {
            return false;
        }
        boolean isSupport = false;
        // this strategy is based on tMRConfiguration_java.xml, Parameter: JOBHISTORY_PRINCIPAL
        if (hadoopVersison.version != null
                && (hadoopVersison.version.equals(EHadoopVersion4Drivers.MICROSOFT_HD_INSIGHT_3_1.getVersionValue()) || hadoopVersison.version
                        .equals(EHadoopVersion4Drivers.MICROSOFT_HD_INSIGHT_3_2.getVersionValue()))) {
            return false;
        } else {
            if (hadoopDistribution.useCustom()) {
                return true;
            } else {
                List<String> supportVersions = new ArrayList<String>();

                supportVersions.add(EHadoopVersion4Drivers.PIVOTAL_HD_1_0_1.getVersionValue());
                supportVersions.add(EHadoopVersion4Drivers.PIVOTAL_HD_2_0.getVersionValue());
                supportVersions.add(EHadoopVersion4Drivers.HDP_2_0.getVersionValue());
                supportVersions.add(EHadoopVersion4Drivers.HDP_2_1.getVersionValue());
                supportVersions.add(EHadoopVersion4Drivers.HDP_2_2.getVersionValue());
                supportVersions.add(EHadoopVersion4Drivers.HDP_2_3.getVersionValue());
                supportVersions.add(EHadoopVersion4Drivers.CLOUDERA_CDH4_YARN.getVersionValue());
                supportVersions.add(EHadoopVersion4Drivers.CLOUDERA_CDH5.getVersionValue());
                supportVersions.add(EHadoopVersion4Drivers.CLOUDERA_CDH5_1.getVersionValue());
                supportVersions.add(EHadoopVersion4Drivers.CLOUDERA_CDH5_4.getVersionValue());
                supportVersions.add(EHadoopVersion4Drivers.CLOUDERA_CDH5_5.getVersionValue());
                supportVersions.add(EHadoopVersion4Drivers.MAPR401.getVersionValue());
                supportVersions.add(EHadoopVersion4Drivers.MAPR500.getVersionValue());
                supportVersions.add(EHadoopVersion4Drivers.APACHE_2_4_0_EMR.getVersionValue());
                supportVersions.add(EHadoopVersion4Drivers.EMR_4_0_0.getVersionValue());

                isSupport = supportVersions.contains(hadoopVersison.version);
            }
        }
        return isSupport;
    }

    private DistributionBean getDistribution() {
        return HadoopDistributionsHelper.HADOOP.getDistribution(getConnection().getDistribution(), false);
    }

    private DistributionVersion getDistributionVersion() {
        final DistributionBean distribution = getDistribution();
        if (distribution != null) {
            return distribution.getVersion(getConnection().getDfVersion(), false);
        }
        return null;
    }

    private boolean isCurrentHadoopVersionSupportGroup() {
        boolean supportGroup = false;
        final DistributionVersion distributionVersion = getDistributionVersion();
        if (distributionVersion != null && distributionVersion.hadoopComponent != null) {
            supportGroup = distributionVersion.hadoopComponent.doSupportGroup();
        }
        return supportGroup;
    }

    private boolean isCustomUnsupportHasGroup() {
        EAuthenticationMode authMode = EAuthenticationMode.getAuthenticationByName(getConnection().getAuthMode(), false);
        return authMode.equals(EAuthenticationMode.UGI);
    }

    private boolean isCustomUnsupportHasSecurity() {
        EAuthenticationMode authMode = EAuthenticationMode.getAuthenticationByName(getConnection().getAuthMode(), false);
        return authMode.equals(EAuthenticationMode.KRB);
    }

    private boolean isCurrentHadoopVersionSupportSecurity() {
        boolean supportSecurity = false;
        final DistributionVersion distributionVersion = getDistributionVersion();
        if (distributionVersion != null && distributionVersion.hadoopComponent != null) {
            supportSecurity = distributionVersion.hadoopComponent.doSupportKerberos();
        }
        return supportSecurity;
    }

    private boolean isCurrentHadoopVersionSupportMapRTicket() {
        boolean supportMapRTicket = false;
        final DistributionVersion distributionVersion = getDistributionVersion();
        if (distributionVersion != null && distributionVersion.hadoopComponent != null) {
            supportMapRTicket = distributionVersion.hadoopComponent.doSupportMapRTicket();
        }
        return supportMapRTicket;
    }

    private boolean isCurrentHadoopVersionSupportYarn() {

        boolean supportYarn = false;
        final DistributionVersion distributionVersion = getDistributionVersion();
        if (distributionVersion != null && distributionVersion.hadoopComponent != null) {
            supportYarn = distributionVersion.hadoopComponent.isHadoop2();
        }
        return supportYarn;
    }

    private void updateMRRelatedContent() {
        boolean useYarn = getConnection().isUseYarn();
        jobtrackerUriText
                .setLabelText(useYarn ? Messages.getString("HadoopClusterForm.text.resourceManager") : Messages.getString("HadoopClusterForm.text.jobtrackerURI")); //$NON-NLS-1$ //$NON-NLS-2$
        jobtrackerUriText.getTextControl().getParent().layout();
        jtOrRmPrincipalText
                .setLabelText(useYarn ? Messages.getString("HadoopClusterForm.text.rmPrincipal") : Messages.getString("HadoopClusterForm.text.jtPrincipal")); //$NON-NLS-1$ //$NON-NLS-2$
        jtOrRmPrincipalText.getTextControl().getParent().layout();
    }

    private void updateConnectionContent() {
        if (!kerberosBtn.isEnabled()) {
            kerberosBtn.setSelection(false);
            namenodePrincipalText.setText(EMPTY_STRING);
            jtOrRmPrincipalText.setText(EMPTY_STRING);
            jobHistoryPrincipalText.setText(EMPTY_STRING);
            getConnection().setEnableKerberos(false);
        }
        if (!maprTBtn.isEnabled()) {
            maprTBtn.setSelection(false);
            maprTPasswordText.setText(EMPTY_STRING);
            maprTClusterText.setText(EMPTY_STRING);
            maprTDurationText.setText(EMPTY_STRING);
            setMaprTHomeDirBtn.setSelection(false);
            setHadoopLoginBtn.setSelection(false);
            preloadAuthentificationBtn.setSelection(false);
            maprTHomeDirText.setText(EMPTY_STRING);
            maprTHadoopLoginText.setText(EMPTY_STRING);
            getConnection().setEnableMaprT(false);
        }
        if (!groupText.getEditable()) {
            groupText.setText(EMPTY_STRING);
        }
        if (!userNameText.getEditable()) {
            userNameText.setText(EMPTY_STRING);
        }
    }

    @Override
    protected void hideControl(Control control, boolean hide) {
        Object layoutData = control.getLayoutData();
        if (layoutData instanceof GridData) {
            GridData data = (GridData) layoutData;
            data.exclude = hide;
            control.setLayoutData(data);
            control.setVisible(!hide);
            if (control.getParent() != null) {
                control.getParent().layout();
            }
        }
    }

    private void fillDefaults() {
        HadoopClusterConnection connection = getConnection();
        if (creation && !connection.isUseCustomConfs()) {
            HCRepositoryUtil.fillDefaultValuesOfHadoopCluster(connection);
        }
    }

    @Override
    public boolean checkFieldsValue() {
        checkServicesBtn.setEnabled(false);

        if (getConnection().isUseCustomVersion()) {
            if (authenticationCombo.getSelectionIndex() == -1) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.authentication")); //$NON-NLS-1$
                return false;
            }
        }

        if (!validText(namenodeUriText.getText())) {
            updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.namenodeURI")); //$NON-NLS-1$
            return false;
        }

        if (!isContextMode() && !HadoopParameterValidator.isValidNamenodeURI(namenodeUriText.getText())) {
            updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.namenodeURI.invalid")); //$NON-NLS-1$
            return false;
        }

        if (!validText(jobtrackerUriText.getText())) {
            updateStatus(IStatus.ERROR,
                    Messages.getString("HadoopClusterForm.check.jobtrackerURI2", jobtrackerUriText.getLabelText())); //$NON-NLS-1$
            return false;
        }

        if (!isContextMode() && !HadoopParameterValidator.isValidJobtrackerURI(jobtrackerUriText.getText())) {
            updateStatus(IStatus.ERROR,
                    Messages.getString("HadoopClusterForm.check.jobtrackerURI.invalid2", jobtrackerUriText.getLabelText())); //$NON-NLS-1$
            return false;
        }

        if (namenodePrincipalText.getEditable()) {
            if (!validText(namenodePrincipalText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.namenodePrincipal")); //$NON-NLS-1$
                return false;
            }
            if (!isContextMode() && !HadoopParameterValidator.isValidPrincipal(namenodePrincipalText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.namenodePrincipal.invalid")); //$NON-NLS-1$
                return false;
            }
        }

        if (jtOrRmPrincipalText.getEditable()) {
            if (!validText(jtOrRmPrincipalText.getText())) {
                updateStatus(IStatus.ERROR,
                        Messages.getString("HadoopClusterForm.check.jtOrRmPrincipal", jtOrRmPrincipalText.getLabelText())); //$NON-NLS-1$
                return false;
            }
            if (!isContextMode() && !HadoopParameterValidator.isValidPrincipal(jtOrRmPrincipalText.getText())) {
                updateStatus(IStatus.ERROR,
                        Messages.getString("HadoopClusterForm.check.jtOrRmPrincipal.invalid", jtOrRmPrincipalText.getLabelText())); //$NON-NLS-1$
                return false;
            }
        }

        if (jobHistoryPrincipalText.getEditable()) {
            if (!validText(jobHistoryPrincipalText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.jobHistoryPrincipal")); //$NON-NLS-1$
                return false;
            }
            if (!isContextMode() && !HadoopParameterValidator.isValidPrincipal(jobHistoryPrincipalText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.jobHistoryPrincipal.invalid")); //$NON-NLS-1$
                return false;
            }
        }

        if (userNameText != null && userNameText.getEditable()) {
            if (!validText(userNameText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.userName")); //$NON-NLS-1$
                return false;
            }
        }

        if (groupText.getEditable()) {
            if (!validText(groupText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.group")); //$NON-NLS-1$
                return false;
            }
            if (!isContextMode() && !HadoopParameterValidator.isValidGroup(groupText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.group.invalid")); //$NON-NLS-1$
                return false;
            }
        }

        if (validText(userNameText.getText()) && !HadoopParameterValidator.isValidUserName(userNameText.getText())) {
            updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.userName.invalid")); //$NON-NLS-1$
            return false;
        }

        if (keytabPrincipalText.getEditable()) {
            if (!validText(keytabPrincipalText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.keytabPrincipal")); //$NON-NLS-1$
                return false;
            }
            if (!isContextMode() && !HadoopParameterValidator.isValidPrincipal(keytabPrincipalText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.keytabPrincipal.invalid")); //$NON-NLS-1$
                return false;
            }
        }

        if (keytabText.getEditable()) {
            if (!validText(keytabText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.keytab")); //$NON-NLS-1$
                return false;
            }
        }

        if (maprTPasswordText.getEditable()) {
            if (!validText(maprTPasswordText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.maprTPassword")); //$NON-NLS-1$
                return false;
            }
            if (!isContextMode() && !HadoopParameterValidator.isValidMaprTPassword(maprTPasswordText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.maprTPassword.invalid")); //$NON-NLS-1$
                return false;
            }
        }

        if (maprTClusterText.getEditable()) {
            if (!validText(maprTClusterText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.maprTCluster")); //$NON-NLS-1$
                return false;
            }
            if (!isContextMode() && !HadoopParameterValidator.isValidMaprTCluster(maprTClusterText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.maprTCluster.invalid")); //$NON-NLS-1$
                return false;
            }
        }

        if (maprTDurationText.getEditable()) {
            if (!validText(maprTDurationText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.maprTDuration")); //$NON-NLS-1$
                return false;
            }
            if (!isContextMode() && !HadoopParameterValidator.isValidMaprTDuration(maprTDurationText.getText())) {
                updateStatus(IStatus.ERROR, Messages.getString("HadoopClusterForm.check.maprTDuration.invalid")); //$NON-NLS-1$
                return false;
            }
        }
        checkServicesBtn.setEnabled(true);
        updateStatus(IStatus.OK, null);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        updateForm();
        if (isReadOnly() != readOnly) {
            adaptFormToReadOnly();
        }
        if (visible) {
            adaptFormToEditable();
            updateStatus(getStatusLevel(), getStatus());
        }
    }

    @Override
    protected void collectConParameters() {
        collectConFieldContextParameters(isCurrentHadoopVersionSupportYarn());
        collectAuthFieldContextParameters(kerberosBtn.getSelection());
        collectKeyTabContextParameters(kerberosBtn.getSelection() && keytabBtn.getSelection());
        collectAuthMaprTFieldContextParameters(maprTBtn.getSelection());
        if (useClouderaNaviBtn != null) {
            collectClouderaNavigatorFieldContextParameters(useClouderaNaviBtn.getSelection());
        }
    }

    private void collectClouderaNavigatorFieldContextParameters(boolean clouderaNav) {
        addContextParams(EHadoopParamName.ClouderaNavigatorUsername, clouderaNav);
        addContextParams(EHadoopParamName.ClouderaNavigatorPassword, clouderaNav);
        addContextParams(EHadoopParamName.ClouderaNavigatorUrl, clouderaNav);
        addContextParams(EHadoopParamName.ClouderaNavigatorMetadataUrl, clouderaNav);
        addContextParams(EHadoopParamName.ClouderaNavigatorClientUrl, clouderaNav);
    }

    private void collectConFieldContextParameters(boolean useYarn) {
        addContextParams(EHadoopParamName.NameNodeUri, true);
        addContextParams(EHadoopParamName.JobTrackerUri, !useYarn);
        addContextParams(EHadoopParamName.ResourceManager, useYarn);
        addContextParams(EHadoopParamName.ResourceManagerScheduler, true);
        addContextParams(EHadoopParamName.JobHistory, true);
        addContextParams(EHadoopParamName.StagingDirectory, true);
    }

    private void collectAuthFieldContextParameters(boolean useKerberos) {
        addContextParams(EHadoopParamName.NameNodePrin, useKerberos);
        addContextParams(EHadoopParamName.JTOrRMPrin, useKerberos);
        addContextParams(EHadoopParamName.JobHistroyPrin, useKerberos);
        addContextParams(EHadoopParamName.User, !useKerberos);
        addContextParams(EHadoopParamName.Group, !useKerberos
                && (isCurrentHadoopVersionSupportGroup() || isCustomUnsupportHasGroup()));
    }

    private void collectKeyTabContextParameters(boolean useKeyTab) {
        addContextParams(EHadoopParamName.Principal, useKeyTab);
        addContextParams(EHadoopParamName.KeyTab, useKeyTab);
    }

    private void collectAuthMaprTFieldContextParameters(boolean useMaprT) {
        addContextParams(EHadoopParamName.maprTPassword, useMaprT);
        addContextParams(EHadoopParamName.maprTCluster, useMaprT);
        addContextParams(EHadoopParamName.maprTDuration, useMaprT);
        addContextParams(EHadoopParamName.maprTHomeDir, useMaprT);
        addContextParams(EHadoopParamName.maprTHadoopLogin, useMaprT);
    }
}
