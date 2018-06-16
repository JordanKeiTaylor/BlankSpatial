/* Copyright Deloitte Consulting LLP 2018 */

package BlankWorkerSupport;

import improbable.collections.Option;
import improbable.Coordinates;
import improbable.Position;
import improbable.PositionData;

import improbable.worker.*;
import improbable.worker.Ops.AddComponent;
import improbable.worker.Ops.AuthorityChange;
import improbable.worker.Ops.RemoveComponent;
import improbable.worker.Ops.AddEntity;
import improbable.worker.Ops.RemoveEntity;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import BlankWorkerSupport.EntityWrapper;
import BlankWorkerSupport.*;
import java.util.ArrayList;

public class LocalView {

    private final Map<EntityId, EntityWrapper> entitiesInView = new HashMap<>();
    private final Map<EntityId, EntityWrapper> entitiesAuthoritativeOverPosition = new HashMap<>();
    protected ArrayList<String> messagesToLogAt5SecondIntervals = new ArrayList<String>();

    private final Connection connection;
    private final Logger logger;
    public Boolean logged = true;


    public LocalView(Connection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
    }


    public void applyWorkerActions() {
        Collection<EntityWrapper> allEntities = this.entitiesAuthoritativeOverPosition.values();
        for(EntityWrapper entity: allEntities){
            entity.randomMovements();
            entity.sendPositionUpdate(connection);
        }

    }

    public void addViewedEntity(AddEntity op){
        EntityId id = op.entityId;
        EntityWrapper entityWrapper = new EntityWrapper(id);
        this.entitiesInView.put(id, entityWrapper);
    }

    public void removeViewedEntity(RemoveEntity op){
        EntityId id = op.entityId;
        EntityWrapper entityWrapper = new EntityWrapper(id);
        this.entitiesInView.remove(id);
    }

    public void addEntityAuthoritativePosition(AuthorityChange op){
        EntityId id = op.entityId;
        EntityWrapper entityWrapper = new EntityWrapper(id);
        this.entitiesAuthoritativeOverPosition.put(id, entityWrapper);
    }

    public void addAuthoritativePositionEntity(AddComponent<PositionData> op){
        EntityId id = op.entityId;
        Coordinates position = op.data.getCoords();
        EntityWrapper entityWrapper = new EntityWrapper(id, position, messagesToLogAt5SecondIntervals, logger, connection);
        this.entitiesAuthoritativeOverPosition.put(id, entityWrapper);
    }



    public void registerDispatcher(Dispatcher dispatcher) {
//        dispatcher.onAddEntity(this::addViewedEntity);
//        dispatcher.onRemoveEntity(this::removeViewedEntity);
        dispatcher.onAddComponent(Position.COMPONENT,this::addAuthoritativePositionEntity);
//        dispatcher.onAuthorityChange(Position.COMPONENT,this::addEntityAuthoritativePosition);
    }





}