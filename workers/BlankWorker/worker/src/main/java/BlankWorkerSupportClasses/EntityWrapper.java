/* Copyright Deloitte Consulting LLP 2018 */

package BlankWorkerSupport;

import improbable.Coordinates;
import improbable.Position;
import improbable.Vector3f;
import improbable.worker.Authority;
import improbable.worker.Connection;
import improbable.worker.EntityId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;
import blankworker.*;


public class EntityWrapper {

    protected final EntityId id;
    private Authority positionAuthoritative;
    private Authority pathAuthoritative;
    protected Coordinates position;
    protected Coordinates destination;
    protected Random random =  new Random();
    protected ArrayList<String> logMessageQueue;
    protected Logger logger;
    protected Connection connection;

    private Position.Update positionUpdate = new Position.Update();
    private Mover.Update destinationUpdate;

    public EntityWrapper(EntityId _id, Coordinates _position, ArrayList<String> _logMessageQueue, Logger _logger, Connection _connection) {
        id = _id;
        position = _position;
        logger = _logger;
        destinationUpdate = new Mover.Update();
        assignNewRandomDestination();
        logMessageQueue =  _logMessageQueue;
        connection = _connection;
    }

    public EntityWrapper(EntityId _id) {
        id = _id;
        assignNewRandomDestination();
    }

    public void setPosition(Coordinates _position) {

        position.setX(_position.getX());
        position.setY(_position.getY());
        position.setZ(_position.getZ());

    }

    public double randomDouble(int min, int max){
        return ThreadLocalRandom.current().nextDouble(min,max);
    }

    public void assignNewRandomDestination(){
        Coordinates newDestination = new Coordinates(randomDouble(-50,50),0,randomDouble(-50,50));
        logger.info("Entity " + id + " assigned destination " + newDestination.getX() + "," + newDestination.getZ());
        destination = newDestination;
//        sendDestinationUpdate(connection,newDestination);
    }

    public void moveTowardsDestination(){
      double x_dist =  destination.getX() - position.getX();
      double z_dist = destination.getZ() - position.getZ();
      double sum = Math.abs(x_dist) + Math.abs(z_dist);
      double x_ratio = Math.abs(x_dist)/sum;
      double x_travel = 0.05 * x_ratio * Math.signum(x_dist);
      double z_travel = 0.05 * (1 - x_ratio) * Math.signum(z_dist);
      Coordinates newPosition = new Coordinates(position.getX() + x_travel, 0,position.getZ() + z_travel);
      setPosition(newPosition);
    }

    public void randomMovements(){
        moveTowardsDestination();
        double x_dist =  destination.getX() - position.getX();
        double z_dist = destination.getZ() - position.getZ();
        if(x_dist<.02 && z_dist <.001){
            assignNewRandomDestination();
            sendDestinationUpdate(connection, destination);
        }
    }


    public void randomMove(){
        Coordinates update = new Coordinates(position.getX() + (random.nextDouble() - .5)/3,
                position.getY() + (random.nextDouble() - .5)/3,
                position.getZ() + (random.nextDouble() - .5)/3);

        setPosition(update);
    }



    public void sendPositionUpdate(Connection connection) {
        positionUpdate.setCoords(position);
        connection.sendComponentUpdate(Position.COMPONENT, id, positionUpdate);
    }

    public void sendDestinationUpdate(Connection connection, Coordinates newDestination) {
        Mover.Update moverUpdate = new Mover.Update();
        moverUpdate.setDestination(newDestination);
        connection.sendComponentUpdate(Mover.COMPONENT, id, moverUpdate);
    }


    public double distance(Coordinates p1, Coordinates p2)
    {

        double squareSum = Math.pow(p1.getX() - p2.getX(), 2.0d) +
                Math.pow(p1.getY() - p2.getY(), 2.0d) +
                Math.pow(p1.getZ() - p2.getZ(), 2.0d);
        if (squareSum >= 0d)
        {
            return Math.pow(squareSum, 0.5d);
        } else
        {
            return 0;
        }
    }


}
