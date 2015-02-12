package org.jboss.tools.openshift.internal.ui.jobs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.openshift.express.internal.ui.OpenShiftUIActivator;
import org.jboss.tools.openshift.express.internal.ui.job.AbstractDelegatingMonitorJob;
import org.jboss.tools.openshift.express.internal.ui.utils.Logger;
import org.jboss.tools.openshift.internal.ui.wizard.deployment.DeploymentWizardContext;

import com.openshift.internal.kube.Resource;
import com.openshift.internal.kube.builders.ImageDeploymentBuilder;
import com.openshift.internal.kube.builders.SourceDeploymentBuilder;
import com.openshift.kube.OpenShiftKubeException;
import com.openshift.kube.ResourceKind;

public class CreateDeploymentJob extends AbstractDelegatingMonitorJob {

	private String sourceUrl;
	private DeploymentWizardContext context;

	public CreateDeploymentJob(String sourceUrl, DeploymentWizardContext context) {
		super("Create Deployment Job");
		this.context = context;
		this.sourceUrl = sourceUrl;
	}

	@Override
	protected IStatus doRun(IProgressMonitor monitor) {
		try {
			List<Resource> resources;
			if(context.includeBuildConfig()){
				SourceDeploymentBuilder builder = new SourceDeploymentBuilder(context.getNamespace(), this.sourceUrl, context.getUserName(),context.getImage().getImageUri(),context.getRepositoryUri());
				builder.serviceDependencies(context.getServiceDependencies());
				resources = builder.build();
			}else{
				ImageDeploymentBuilder builder = new ImageDeploymentBuilder(context.getNamespace(), context.getImage().getImageUri(), 27017);
				builder.addEnvironmentVariables(context.getImage().getEnvironmentVariables());
				builder.serviceDependencies(context.getServiceDependencies());
				resources = builder.build();
			}
			
			List<com.openshift.kube.Status> errors = new ArrayList<com.openshift.kube.Status>();
			for (Resource r : resources) {
				Logger.debug("Creating Resource: " + r.toPrettyString());
				createResource(errors, r);
			}
			//trigger a deployment here? maybe post a build? post a deployment?

			if(!errors.isEmpty()){
				//TODO fix me 
				return new Status(IStatus.ERROR, OpenShiftUIActivator.PLUGIN_ID,  IStatus.ERROR,errors.toString(),null);
			}
			return new Status(IStatus.OK, OpenShiftUIActivator.PLUGIN_ID, OK, "Complete", null);
			// catch here?
		} catch (Exception e){
			return new Status(IStatus.ERROR, OpenShiftUIActivator.PLUGIN_ID, IStatus.ERROR,"",e);
		}
	}
	
	private void createResource(List<com.openshift.kube.Status> errors, Resource resource) {
		Resource response = context.getClient().create(resource);
		if(ResourceKind.Status == response.getKind()){
			errors.add((com.openshift.kube.Status) response);
		}
	}

}
