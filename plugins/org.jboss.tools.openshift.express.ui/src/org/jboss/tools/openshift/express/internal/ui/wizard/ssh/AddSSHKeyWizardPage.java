/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.express.internal.ui.wizard.ssh;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.common.ui.WizardUtils;
import org.jboss.tools.common.ui.databinding.ValueBindingBuilder;
import org.jboss.tools.openshift.express.internal.core.console.UserDelegate;
import org.jboss.tools.openshift.express.internal.ui.OpenShiftUIActivator;
import org.jboss.tools.openshift.express.internal.ui.databinding.RequiredControlDecorationUpdater;
import org.jboss.tools.openshift.express.internal.ui.utils.SSHUtils;
import org.jboss.tools.openshift.express.internal.ui.wizard.AbstractOpenShiftWizardPage;

/**
 * @author André Dietisheim
 */
public class AddSSHKeyWizardPage extends AbstractOpenShiftWizardPage {

	private AddSSHKeyWizardPageModel pageModel;

	public AddSSHKeyWizardPage(UserDelegate user, IWizard wizard) {
		super("Add existing SSH Key", "Add an exiting SSH key to your OpenShift account",
				"AddSSHKeysPage", wizard);
		this.pageModel = new AddSSHKeyWizardPageModel(user);
	}

	@Override
	protected void doCreateControls(Composite parent, DataBindingContext dbc) {
		GridLayoutFactory.fillDefaults()
				.margins(10, 10).applyTo(parent);

		Group addSSHKeyGroup = new Group(parent, SWT.NONE);
		addSSHKeyGroup.setText("Add existing SSH Key");
		GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(addSSHKeyGroup);
		GridLayoutFactory.fillDefaults()
				.numColumns(3).margins(6, 6).applyTo(addSSHKeyGroup);

		Label nameLabel = new Label(addSSHKeyGroup, SWT.NONE);
		nameLabel.setText("Name:");
		GridDataFactory.fillDefaults()
				.align(SWT.LEFT, SWT.CENTER).applyTo(nameLabel);

		Text nameText = new Text(addSSHKeyGroup, SWT.BORDER);
		GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(nameText);
		Binding nameBinding = ValueBindingBuilder
				.bind(WidgetProperties.text(SWT.Modify).observe(nameText))
				.validatingAfterConvert(new SSHPublicKeyNameValidator(pageModel))
				.to(BeanProperties.value(AddSSHKeyWizardPageModel.PROPERTY_NAME).observe(pageModel))
				.notUpdatingParticipant()
				.in(dbc);
		ControlDecorationSupport.create(
				nameBinding, SWT.LEFT | SWT.TOP, null, new RequiredControlDecorationUpdater());

		Label publicKeyLabel = new Label(addSSHKeyGroup, SWT.NONE);
		GridDataFactory.fillDefaults()
				.align(SWT.LEFT, SWT.CENTER).applyTo(publicKeyLabel);
		publicKeyLabel.setText("Public Key:");

		Text publicKeyText = new Text(addSSHKeyGroup, SWT.BORDER);
		publicKeyText.setEditable(false);
		GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(publicKeyText);
		IObservableValue publicKeyObservable =
				WidgetProperties.text(SWT.Modify).observe(publicKeyText);
		ValueBindingBuilder
				.bind(publicKeyObservable)
				.to(BeanProperties.value(AddSSHKeyWizardPageModel.PROPERTY_PUBLICKEY_PATH).observe(pageModel))
				.in(dbc);

		Button browseButton = new Button(addSSHKeyGroup, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(onBrowse());
		GridDataFactory.fillDefaults()
				.align(SWT.FILL, SWT.CENTER).applyTo(browseButton);

		ValidationStatusProvider sshPublicKeyValidator = new SSHPublicKeyValidator(publicKeyObservable, pageModel);
		dbc.addValidationStatusProvider(sshPublicKeyValidator);
		ControlDecorationSupport.create(
				sshPublicKeyValidator, SWT.LEFT | SWT.TOP, null, new RequiredControlDecorationUpdater());
	}

	private SelectionListener onBrowse() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setFilterPath(SSHUtils.getSSH2Home());
				String filePath = null;
				if ((filePath = dialog.open()) != null) {
					pageModel.setPublicKeyPath(filePath);
				}
			}
		};
	}

	public IStatus addConfiguredSSHKey() {
		try {
			return WizardUtils.runInWizard(new AddSSHKeyJob(pageModel), getContainer());
		} catch (Exception e) {
			return OpenShiftUIActivator.createErrorStatus("Could not add ssh key " + pageModel.getName() + ".");
		}
	}
}