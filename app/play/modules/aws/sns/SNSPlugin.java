package play.modules.aws.sns;

import java.util.List;

import play.Application;
import play.Configuration;
import play.Logger;
import play.Plugin;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;

public class SNSPlugin extends Plugin {
	
	private final Application application;

    private AmazonSNSClient client;

    public SNSPlugin(Application application) {
        this.application = application;
    }
    
    @Override
    public void onStart() {
        Configuration aws = Configuration.root().getConfig("aws");
        Configuration sns = Configuration.root().getConfig("sns");
        if (aws != null && sns != null) {
            String accesskey = aws.getString("accesskey");
            String secretkey = aws.getString("secretkey");
            String endpoint = sns.getString("endpoint");
            List<String> topics = sns.getStringList("topics");
            if (accesskey != null && secretkey != null && endpoint != null) {
                AWSCredentials credentials = new BasicAWSCredentials(accesskey, secretkey);
                client = new AmazonSNSClient(credentials);
                client.setEndpoint(endpoint);
                for (String topic : topics) {
                	client.createTopic(new CreateTopicRequest(topic));
                }
            }
        }
        Logger.info("SNSPlugin has started");
    }

    @Override
    public void onStop() {
        Logger.info("SNSPlugin has stopped");
    }
	
	public AmazonSNSClient client() {
        return client;
    }

}
