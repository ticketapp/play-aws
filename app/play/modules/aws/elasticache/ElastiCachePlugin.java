package play.modules.aws.elasticache;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticache.AmazonElastiCache;
import com.amazonaws.services.elasticache.AmazonElastiCacheClient;
import com.amazonaws.services.elasticache.model.CacheCluster;
import com.amazonaws.services.elasticache.model.CacheNode;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersRequest;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersResult;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import play.Application;
import play.Configuration;
import play.Logger;
import play.api.cache.CacheAPI;
import play.api.cache.CachePlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElastiCachePlugin extends CachePlugin {

	private final Application application;
	
	private ElastiCache memcached;
	
	public ElastiCachePlugin(Application application) {
        this.application = application;
    }
    
    public void onStart() {
        Configuration aws = Configuration.root().getConfig("aws");
        Configuration elasticache = Configuration.root().getConfig("elasticache");
        if (aws != null && elasticache != null) {
            String accesskey = aws.getString("accesskey");
            String secretkey = aws.getString("secretkey");
            String clusterid = elasticache.getString("cluster");
            if (accesskey != null && secretkey != null && clusterid != null) {
            	
            	// elasticache client
                AWSCredentials credentials = new BasicAWSCredentials(accesskey, secretkey);
                AmazonElastiCache elasticacheClient = new AmazonElastiCacheClient(credentials);
                String endpoint = elasticache.getString("endpoint");
                if (endpoint != null) {
                    elasticacheClient.setEndpoint(endpoint);
                }
                
                // retrieve elasticache endpoints
                List<String> endpoints = new ArrayList<String>();
                DescribeCacheClustersRequest request = new DescribeCacheClustersRequest();
                request.setShowCacheNodeInfo(true);
                request.setCacheClusterId(clusterid);
                DescribeCacheClustersResult result = elasticacheClient.describeCacheClusters(request);
                List<CacheCluster> clusters = result.getCacheClusters();
                for (CacheCluster cluster : clusters) {
                    List<CacheNode> nodes = cluster.getCacheNodes();
                    for (CacheNode node : nodes) {
                    	endpoints.add(node.getEndpoint().getAddress());
                    }
                }
                
                // create the memcached client
                ConnectionFactoryBuilder connectionFactoryBuilder = new ConnectionFactoryBuilder();
                try {
					memcached = new ElastiCache(new MemcachedClient(connectionFactoryBuilder.build(), AddrUtil.getAddresses(endpoints)));
                } catch (IOException e) {
					throw new RuntimeException("ElastiCachePlugin configuration problem");
				}
            }
        }
        
        Logger.info("ElastiCachePlugin has started");
    }

    public void onStop() {
        Logger.info("ElastiCachePlugin has stopped");
    }

    @Override
	public CacheAPI api() {
		return memcached;
	}
	
	
}
