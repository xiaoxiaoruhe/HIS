package com.zeroone.cloud.fastdfs.factory;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

/**
 * <p>
 * 描述：用于创建连接对象的工厂类
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 2.0.0
 */
public class StorageClientFactory implements PooledObjectFactory<StorageClient> {

    @Override
    public PooledObject<StorageClient> makeObject() throws Exception {
        TrackerClient client = new TrackerClient();
        TrackerServer server = client.getTrackerServer();
        return new DefaultPooledObject<>(new StorageClient(server, null));
    }

    @Override
    public void destroyObject(PooledObject<StorageClient> p) throws Exception {
        p.getObject().close();
    }

    @Override
    public boolean validateObject(PooledObject<StorageClient> p) {
        return false;
    }

    @Override
    public void activateObject(PooledObject<StorageClient> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<StorageClient> p) throws Exception {

    }
}
