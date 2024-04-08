package com.protohackers.speed;

import java.util.*;

public class CarObservatory {

    public record CarSighting(long mile, long timestamp, long limit) {}
    private final Map<String, Map<Long, List<CarSighting>>> sightings = new HashMap<>(); // plate => (road => ())
    private final Set<String> finedCars = new HashSet<>();


    public void addCarSighting(String plate, long timestamp, long road, long mile, long limit) {
        synchronized (sightings) {
            sightings.computeIfAbsent(plate, k -> new HashMap<>());
            sightings.get(plate).computeIfAbsent(road, k -> new ArrayList<>());
            sightings.get(plate).get(road).add(new CarSighting(mile, timestamp, limit));
            sightings.get(plate).get(road).sort(Comparator.comparingLong((CarSighting s) -> s.timestamp));
        }
    }

    public List<Message> issueTickets() {
        var ret = new ArrayList<Message>();
        for (String plate : sightings.keySet()) {
            for (Long road : sightings.get(plate).keySet()) {
                var list = sightings.get(plate).get(road);
                if (list.size() < 2)
                    continue;

                for (int i = 1; i < list.size(); i++) {
                    CarSighting currCamera = list.get(i);
                    CarSighting prevCamera = list.get(i-1);
                    long distance =  currCamera.mile - prevCamera.mile;
                    double timeDiff = (double) (currCamera.timestamp - prevCamera.timestamp) / 3600;
                    if (distance < 0 || timeDiff < 0)
                        continue;
                    long speedBetweenCameras = Math.round( (double) distance / timeDiff) * 100;
                    if (speedBetweenCameras > currCamera.limit * 100 && !finedCars.contains(plate)) {
                        finedCars.add(plate);
                        ret.add(MessageIO.createTicketMessage(
                                plate, road,
                                prevCamera.mile, prevCamera.timestamp, currCamera.mile, currCamera.timestamp,
                                speedBetweenCameras
                        ));
                    }
                }
            }
        }
        return ret;
    }
}
