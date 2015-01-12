package org.jboss.tools.openshift.internal.ui.property;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.openshift.kube.DeploymentConfig;

public class DeploymentConfigPropertySource implements IPropertySource{
	
	private final DeploymentConfig config;
	private final Map<Object, Object> values = new HashMap<Object,Object>();
	public DeploymentConfigPropertySource(DeploymentConfig config){
		this.config = config;
		values.put("name", config.getName());
		values.put("namespace", config.getNamespace());
		values.put("createdOn", config.getCreationTimeStamp());
//		values.put("sourceUrl","");
		values.put("url", "TBD");
		values.put("deploymentTriggers", StringUtils.join(config.getTriggerTypes().toArray()));
//		values.put("buildTriggers", "");
//		values.put("baseImage", "");
		values.put("deployImage", config.getImageNames());
//		values.put("buildType", "");
		values.put("replicas", config.getReplicas());
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return new IPropertyDescriptor[]{
				new PropertyDescriptor("name", "Name"),
				new PropertyDescriptor("namespace", "NameSpace"),
				new PropertyDescriptor("createdOn", "Created On"),
				new PropertyDescriptor("sourceUrl", "Source URL"),
				new PropertyDescriptor("url", "Public URL"),
				new PropertyDescriptor("deploymentTriggers", "Deployment Triggers"),
				new PropertyDescriptor("buildTriggers", "Build Triggers"),
				new PropertyDescriptor("baseImage", "Base Image"),
				new PropertyDescriptor("deployImage", "Deployment Images"),
				new PropertyDescriptor("buildType", "Build Type"),
				new PropertyDescriptor("replicas", "Replicas")
		};
	}

	@Override
	public Object getPropertyValue(Object id) {
		return values.get(id);
	}

	// Unimplemented methods
	
	@Override
	public Object getEditableValue() {
		return null;
	}


	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {
	}

	@Override
	public void setPropertyValue(Object id, Object value) {
	}

}
