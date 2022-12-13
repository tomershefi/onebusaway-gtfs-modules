package org.onebusaway.gtfs.serialization;

import static junit.framework.Assert.assertEquals;
import static org.onebusaway.gtfs.serialization.GtfsReaderTest.processFeed;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import org.junit.Test;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.MockGtfs;

/**
 * The commit https://github.com/MobilityData/gtfs-flex/commit/547200dfb580771265ae14b07d9bfd7b91c16ed2
 * of the flex V2 spec changes the following spellings :
 *
 *  - start_pickup_dropoff_window -> start_pickup_drop_off_window
 *  - end_pickup_dropoff_window -> start_pickup_drop_off_window
 *
 * Since it's hard to spot: the change is in the word "dropoff" vs "drop_off".
 *
 * This test makes sure that both spellings are understood.
 */
public class FlexDropOffSpellingTest {

  @Test
  public void oldSpelling() throws IOException {
    MockGtfs gtfs = MockGtfs.create();
    gtfs.putMinimal();
    gtfs.putDefaultTrips();
    gtfs.putLines(
            "stop_times.txt",
            "trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_booking_rule_id,drop_off_booking_rule_id,start_pickup_dropoff_window,end_pickup_dropoff_window",
            "T10-0,,,location-123,0,headsign-1,,,10:00:00,18:00:00"
    );
    GtfsRelationalDao dao = processFeed(gtfs.getPath(), "1", false);

    assertEquals(1, dao.getAllStopTimes().size());

    StopTime stopTime = List.copyOf(dao.getAllStopTimes()).get(0);

    assertEquals("1_T10-0", stopTime.getTrip().getId().toString());
    assertEquals(LocalTime.parse("10:00").toSecondOfDay(), stopTime.getStartPickupDropOffWindow());
    assertEquals(LocalTime.parse("18:00").toSecondOfDay(), stopTime.getEndPickupDropOffWindow());
  }
}
