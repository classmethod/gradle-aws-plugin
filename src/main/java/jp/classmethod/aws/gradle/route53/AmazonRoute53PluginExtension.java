package jp.classmethod.aws.gradle.route53;

import java.util.ArrayList;
import java.util.List;

import jp.classmethod.aws.gradle.AwsPluginExtension;
import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.AliasTarget;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.ListResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ListResourceRecordSetsResult;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecordSet;


public class AmazonRoute53PluginExtension {
	
	public static final String NAME = "route53";
			
	@Getter @Setter
	private	Project project;
	
	@Getter @Setter
	private	String profileName;
	
	@Getter @Setter
	private String region = Regions.US_EAST_1.getName();
			
	@Getter @Setter
	String hostedZone;
	
	@Getter @Setter
	String callerReference;
	
	@Getter(lazy = true)
	private final AmazonRoute53 client = initClient();

	public AmazonRoute53PluginExtension(Project project) {
		this.project = project;
	}

	private AmazonRoute53 initClient() {
		AwsPluginExtension aws = project.getExtensions().getByType(AwsPluginExtension.class);
		return aws.createClient(
				AmazonRoute53Client.class,
				this.region == null ? null : RegionUtils.getRegion(this.region),
				profileName);
	}
	
	public String getHostedZoneId() {
		ListHostedZonesResult lhzr = getClient().listHostedZones();
		HostedZone zone = lhzr.getHostedZones().stream().filter(it -> it.getName().equals(hostedZone + ".")).findFirst().orElse(null);
		if (zone == null) {
			throw new GradleException("Hosted zone "+hostedZone+" not found.");
		}
		return zone.getId();
	}
	
	public ResourceRecordSet getAssociatedResourceRecordSet(String hostname) {
		String resourceRecordName = hostname.replace(hostedZone, "");
		ListResourceRecordSetsResult lrrsr = getClient().listResourceRecordSets(new ListResourceRecordSetsRequest(getHostedZoneId())
			.withStartRecordName(resourceRecordName));
		return lrrsr.getResourceRecordSets().stream()
				.filter(it -> it.getType().equals("CNAME") || it.getAliasTarget() != null).findFirst().get();
	}
	
	public void associateAsAlias(String hostname, LoadBalancerDescription ldb, ResourceRecordSet oldResourceRecordSet) {
		List<Change> changes = new ArrayList<>();
		if (oldResourceRecordSet != null) {
			changes.add(new Change(ChangeAction.DELETE, oldResourceRecordSet));
		}
		changes.add(new Change(ChangeAction.CREATE, new ResourceRecordSet(hostname, RRType.A)
			.withAliasTarget(new AliasTarget(ldb.getCanonicalHostedZoneNameID(), ldb.getCanonicalHostedZoneName())
				.withEvaluateTargetHealth(false))));
		
		getClient().changeResourceRecordSets(new ChangeResourceRecordSetsRequest()
			.withHostedZoneId(getHostedZoneId())
			.withChangeBatch(new ChangeBatch().withChanges(changes)));
	}
	
	public void swapAlias(String hostname, List<LoadBalancerDescription> ldbs, ResourceRecordSet oldResourceRecordSet) {
		String oldDNSName = oldResourceRecordSet == null ? null : oldResourceRecordSet.getAliasTarget().getDNSName().toLowerCase();
		System.out.println("oldDNSName = " + oldDNSName);
		LoadBalancerDescription ldb = ldbs.stream()
				.filter(it -> (it.getDNSName().toLowerCase() + ".").equals(oldDNSName) == false)
				.findFirst().get();
		System.out.println("newDNSName = "+ldb.getDNSName().toLowerCase()+".");
		associateAsAlias(hostname, ldb, oldResourceRecordSet);
	}
}
