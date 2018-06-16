package BlankWorkerSupport;


import improbable.Coordinates;
import improbable.Position;
import improbable.Vector3f;
import improbable.worker.Authority;
import improbable.worker.Connection;
import improbable.worker.EntityId;
import improbable.*;
import improbable.worker.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;
import blankworker.*;

public class EntityFactory{
    private Connection connection;
    private Dispatcher dispatcher;
    private Long dispatcherReservationKey;
    private RequestId<CreateEntityRequest> entityCreationRequestId;
    private RequestId<ReserveEntityIdRequest> reserveEntityRequestId;
    final improbable.collections.Option<Integer> timeoutMillis = improbable.collections.Option.of(500);

    public EntityFactory(Connection _connection, Dispatcher _dispatcher){
        connection = _connection;
        dispatcher = _dispatcher;
    }

    public void registerEntityWithDispatcher(Entity entity){

        dispatcherReservationKey = dispatcher.onReserveEntityIdResponse(op -> {
            if (op.requestId.equals(reserveEntityRequestId) && op.statusCode == StatusCode.SUCCESS) {
//                Entity entity = new Entity();
//                entity.add(improbable.Metadata.COMPONENT, new improbable.MetadataData(entityType));
//                // Empty ACL - should be customised.
//                entity.add(improbable.EntityAcl.COMPONENT, new improbable.EntityAclData(new improbable.WorkerRequirementSet(new java.util.ArrayList<>()), new java.util.HashMap<>()));
//                // Needed for the entity to be persisted in snapshots.
//                entity.add(improbable.Persistence.COMPONENT, new improbable.PersistenceData());
//                entity.add(improbable.Position.COMPONENT, new improbable.PositionData(new improbable.Coordinates(1, 2, 3)));
                entityCreationRequestId =
                        connection.sendCreateEntityRequest(entity, op.entityId, timeoutMillis);
            }
        });

        dispatcher.onCreateEntityResponse(op -> {
            if (op.requestId.equals(entityCreationRequestId) && op.statusCode == StatusCode.SUCCESS) {
               unregister();
            }
        });
    }

    public void unregister(){
        dispatcher.remove(dispatcherReservationKey);
    }

    public void makeEntity(Entity entity){
        registerEntityWithDispatcher(entity);
        reserveEntityRequestId = connection.sendReserveEntityIdRequest(timeoutMillis);

    }
}