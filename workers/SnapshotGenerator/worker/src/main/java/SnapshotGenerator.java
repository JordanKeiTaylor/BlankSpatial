/* Copyright Deloitte Consulting LLP 2018 */

import improbable.Metadata;
import improbable.worker.*;
import improbable.*;
import improbable.worker.Entity;
import improbable.worker.EntityId;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import blankworker.Mover;
import blankworker.MoverData;
import java.util.Map;

public class SnapshotGenerator {

    public static final WorkerAttributeSet blankAttributes = new WorkerAttributeSet(Collections.singletonList("blank"));
    public static final WorkerRequirementSet blankReqs = new WorkerRequirementSet(Collections.singletonList(blankAttributes));

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Unexpected number of arguments. Usage: " +
                    "spatial local worker launch SnapshotGenerator default path/to/my/snapshotfile.snapshot");
        } else {
            buildSnapshot(args[0]);
        }
    }

    private static void buildSnapshot(String outputPath) {

        Map<EntityId, Entity> entities = generateEntities();
        saveSnapshot(entities, outputPath);
    }

    private static Map<EntityId, Entity> generateEntities(){
        Map<EntityId, Entity> entities = new HashMap<>();
        for (int x = 0; x <= 6; x++){
            Entity entity = new Entity();
            entity.add(Position.COMPONENT, new PositionData(new improbable.Coordinates(1, 2, 3)));
            entity.add(Persistence.COMPONENT, new PersistenceData());
            entity.add(Metadata.COMPONENT, new MetadataData("Spark"));
            entity.add(Metadata.COMPONENT, new MetadataData("Spark"));
            entity.add(Mover.COMPONENT, new MoverData(new improbable.Coordinates(1, 2, 3)));

            Map<Integer, WorkerRequirementSet> componentWriteAuthority = new HashMap<>();
            componentWriteAuthority.put(Position.COMPONENT_ID, blankReqs);
            componentWriteAuthority.put(Mover.COMPONENT_ID, blankReqs);

            entity.add(EntityAcl.COMPONENT, new EntityAclData(blankReqs, componentWriteAuthority));
            entities.put(new EntityId(x + 1),entity);
        }
        return entities;

    }

    private static void saveSnapshot(Map<EntityId, Entity> snapshot, String outputPath) {

//        SnapshotOutputStream outputStream = new SnapshotOutputStream(outputPath);
//
//        for (EntityId id : snapshot.keySet()) {
//            System.out.println(snapshot.get(id));
//            System.out.println(outputStream.writeEntity(id, snapshot.get(id)));
//        }
//
//        outputStream.close();
//        System.out.println("Wrote snapshot file to \"" + outputPath + "\"  - wrote (" + snapshot.size() + " entities)");


        Snapshot.save(outputPath, snapshot);

        System.out.println("Wrote snapshot file to \"" + outputPath + "\"  - wrote (" + snapshot.size() + " entities)");

    }
}
