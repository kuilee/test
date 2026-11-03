/*
We are building the back-end for a clinic appointment management system. The system tracks doctors and their appointments.

Definitions:
* A "Doctor" has an ID and a name.
* An "Appointment" has an ID, a doctor ID, a patient ID, duration in minutes, a status (SCHEDULED, COMPLETED, CANCELLED, or NO_SHOW), and a type (CONSULTATION, FOLLOWUP, or EMERGENCY).
* A "ClinicManager" manages doctors and appointments and provides appointment statistics.

To begin with, two tasks:
1) Read through and understand the code below. Take as much time as you need, and feel free to run the code.
2) The test for ClinicManager is not passing due to a bug in the code. Make the necessary changes to ClinicManager to fix the bug.
*/

/*
Now that the platform can track appointment statistics, we want to look up appointments by patient and report on appointment durations by type.

2-1) Add getAppointmentsByPatient() to ClinicManager. This function takes a patient ID and returns a list of the IDs of all of that patient's appointments across all doctors, regardless of status. The list is sorted by appointment ID in ascending order. Return an empty list if the patient has no appointments.

2-2) Add getAverageAppointmentDurationByType() to ClinicManager. This function takes a doctor ID and returns a map from each appointment type (CONSULTATION, FOLLOWUP, EMERGENCY) to the doctor's average appointment duration in minutes for that type. Only appointments with a COMPLETED status should be counted; ignore appointments with any other status. Include an appointment type in the map only if the doctor has at least one completed appointment of that type. Return an empty map if the doctor has no completed appointments.

The testGetAppointmentsByPatient and testGetAverageAppointmentDurationByType tests are provided.
*/


/*
Now that the platform can track appointment statistics, we want to generate an appointment activity summary for each doctor.

3) Add getDoctorAppointmentSummary() to ClinicManager. This function returns a map from each doctorId to that doctor's DoctorAppointmentSummary.

   The DoctorAppointmentSummary class has been added to represent this summary:
     - totalAppointments: total number of appointments for that doctor
     - totalMinutes: sum of the duration in minutes of all appointments for that doctor
     - busiestAppointmentType: the appointment type with the most appointments for that doctor. If two appointment types are tied, pick the one that comes first alphabetically. If the doctor has no appointments, set this to null.

   All of a doctor's appointments count toward the summary, regardless of status. The result should include every doctor in the system, including doctors who have no appointments. For a doctor with no appointments, return a summary with totalAppointments set to 0, totalMinutes set to 0, and busiestAppointmentType set to null.

The testGetDoctorAppointmentSummary test is provided.
*/

import java.util.*;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;

enum AppointmentStatus {
    SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
}

enum AppointmentType {
    CONSULTATION, FOLLOWUP, EMERGENCY
}

class Doctor {
    public int doctorId;
    public String name;

    public Doctor(int doctorId, String name) {
        this.doctorId = doctorId;
        this.name = name;
    }
}

class Appointment {
    public int appointmentId;
    public int doctorId;
    public int patientId;
    public int durationMinutes;
    public AppointmentStatus status;
    public AppointmentType appointmentType;

    public Appointment(int appointmentId, int doctorId, int patientId, int durationMinutes,
                       AppointmentStatus status, AppointmentType appointmentType) {
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.appointmentType = appointmentType;
    }
}

class AppointmentStats {
    public int totalAppointments;
    public int completedAppointments;
    public double noShowRate;

    public AppointmentStats(int totalAppointments, int completedAppointments, double noShowRate) {
        this.totalAppointments = totalAppointments;
        this.completedAppointments = completedAppointments;
        this.noShowRate = noShowRate;
    }
}

class DoctorAppointmentSummary {
    public int totalAppointments;
    public int totalMinutes;
    public AppointmentType busiestAppointmentType;

    public DoctorAppointmentSummary(int totalAppointments, int totalMinutes,
                                    AppointmentType busiestAppointmentType) {
        this.totalAppointments = totalAppointments;
        this.totalMinutes = totalMinutes;
        this.busiestAppointmentType = busiestAppointmentType;
    }
}

class ClinicManager {
    public Map<Integer, Doctor> doctors = new HashMap<>();
    public List<Appointment> appointments = new ArrayList<>();

    public void addDoctor(Doctor doctor) {
        doctors.put(doctor.doctorId, doctor);
    }

    public void addAppointment(Appointment appointment) {
        if (!doctors.containsKey(appointment.doctorId)) {
            return;
        }
        appointments.add(appointment);
    }

    public AppointmentStats getAppointmentStatistics() {
        /*
            Return statistics across all appointments:
            * totalAppointments:     total number of appointments
            * completedAppointments: number of appointments with status COMPLETED
            * noShowRate:            NO_SHOW appointments / totalAppointments, expressed as a value between 0.0 and 1.0
        */
        int totalAppointments = appointments.size();

        int completedAppointments = 0;
        for (Appointment a : appointments) {
            if (a.status == AppointmentStatus.COMPLETED || a.status == AppointmentStatus.NO_SHOW) {
                completedAppointments++;
            }
        }

        int noShows = 0;
        for (Appointment a : appointments) {
            if (a.status == AppointmentStatus.NO_SHOW) {
                noShows++;
            }
        }

        double noShowRate;
        if (totalAppointments > 0) {
            noShowRate = (double) noShows / totalAppointments;
        } else {
            noShowRate = 0.0;
        }

        return new AppointmentStats(totalAppointments, completedAppointments, noShowRate);
    }
}


public class Solution {
    public static class TestSuite {
        @Test
        public void testGetAppointmentStatistics() {
            ClinicManager cm = new ClinicManager();
            cm.addDoctor(new Doctor(10, "dr_smith"));

            cm.addAppointment(new Appointment(1, 10, 100, 30, AppointmentStatus.COMPLETED, AppointmentType.CONSULTATION));
            cm.addAppointment(new Appointment(2, 10, 101, 45, AppointmentStatus.COMPLETED, AppointmentType.FOLLOWUP));
            cm.addAppointment(new Appointment(3, 10, 102, 30, AppointmentStatus.NO_SHOW, AppointmentType.EMERGENCY));
            cm.addAppointment(new Appointment(4, 10, 103, 60, AppointmentStatus.CANCELLED, AppointmentType.CONSULTATION));
            cm.addAppointment(new Appointment(5, 10, 104, 30, AppointmentStatus.SCHEDULED, AppointmentType.FOLLOWUP));

            AppointmentStats stats = cm.getAppointmentStatistics();
            assertEquals(5, stats.totalAppointments);
            assertEquals(2, stats.completedAppointments);
            assertTrue(Math.abs(stats.noShowRate - 0.2) < 1e-4);

            // A second clinic where every appointment is a no-show.
            ClinicManager cm2 = new ClinicManager();
            cm2.addDoctor(new Doctor(20, "dr_jones"));
            cm2.addAppointment(new Appointment(1, 20, 200, 30, AppointmentStatus.NO_SHOW, AppointmentType.CONSULTATION));
            cm2.addAppointment(new Appointment(2, 20, 201, 30, AppointmentStatus.NO_SHOW, AppointmentType.FOLLOWUP));

            AppointmentStats stats2 = cm2.getAppointmentStatistics();
            assertEquals(2, stats2.totalAppointments);
            assertEquals(0, stats2.completedAppointments);
            assertTrue(Math.abs(stats2.noShowRate - 1.0) < 1e-4);
        }
        
            // Add inside the existing TestSuite class:
    @Test
    public void testGetAppointmentsByPatient() {
        ClinicManager cm = new ClinicManager();
        cm.addDoctor(new Doctor(1, "dr_smith"));
        cm.addDoctor(new Doctor(2, "dr_jones"));

        // Patient 500 has appointments with both doctors, in every status, added out of ID order.
        cm.addAppointment(new Appointment(10, 1, 500, 30, AppointmentStatus.COMPLETED, AppointmentType.CONSULTATION));
        cm.addAppointment(new Appointment(2, 2, 500, 20, AppointmentStatus.CANCELLED, AppointmentType.FOLLOWUP));
        cm.addAppointment(new Appointment(9, 1, 500, 15, AppointmentStatus.NO_SHOW, AppointmentType.EMERGENCY));

        // A different patient, and a scheduled appointment for patient 500.
        cm.addAppointment(new Appointment(4, 1, 501, 30, AppointmentStatus.COMPLETED, AppointmentType.CONSULTATION));
        cm.addAppointment(new Appointment(7, 2, 500, 45, AppointmentStatus.SCHEDULED, AppointmentType.CONSULTATION));

        assertEquals(Arrays.asList(2, 7, 9, 10), cm.getAppointmentsByPatient(500));
        assertEquals(Arrays.asList(4), cm.getAppointmentsByPatient(501));

        // Patient with no appointments.
        assertTrue(cm.getAppointmentsByPatient(999).isEmpty());
    }

    @Test
    public void testGetAverageAppointmentDurationByType() {
        ClinicManager cm = new ClinicManager();
        cm.addDoctor(new Doctor(1, "dr_smith"));
        cm.addDoctor(new Doctor(2, "dr_jones"));
        cm.addDoctor(new Doctor(3, "dr_brown"));
        cm.addDoctor(new Doctor(4, "dr_lee"));
        cm.addDoctor(new Doctor(5, "dr_kim"));
        cm.addDoctor(new Doctor(6, "dr_wong"));

        // Doctor 1: three completed consultations and one completed follow-up.
        cm.addAppointment(new Appointment(1, 1, 100, 30, AppointmentStatus.COMPLETED, AppointmentType.CONSULTATION));
        cm.addAppointment(new Appointment(2, 1, 101, 41, AppointmentStatus.COMPLETED, AppointmentType.CONSULTATION));
        cm.addAppointment(new Appointment(3, 1, 102, 20, AppointmentStatus.COMPLETED, AppointmentType.FOLLOWUP));
        cm.addAppointment(new Appointment(4, 1, 103, 56, AppointmentStatus.COMPLETED, AppointmentType.CONSULTATION));

        // Not COMPLETED -> excluded from the averages (including the no-show).
        cm.addAppointment(new Appointment(5, 1, 105, 100, AppointmentStatus.CANCELLED, AppointmentType.CONSULTATION));
        cm.addAppointment(new Appointment(6, 1, 200, 220, AppointmentStatus.SCHEDULED, AppointmentType.CONSULTATION));
        cm.addAppointment(new Appointment(7, 1, 201, 90, AppointmentStatus.NO_SHOW, AppointmentType.CONSULTATION));

        // Doctor 1 has an emergency appointment, but it was cancelled.
        cm.addAppointment(new Appointment(8, 1, 202, 45, AppointmentStatus.CANCELLED, AppointmentType.EMERGENCY));
        cm.addAppointment(new Appointment(9, 2, 103, 40, AppointmentStatus.COMPLETED, AppointmentType.EMERGENCY));
        cm.addAppointment(new Appointment(10, 4, 106, 25, AppointmentStatus.COMPLETED, AppointmentType.CONSULTATION));
        cm.addAppointment(new Appointment(11, 5, 107, 15, AppointmentStatus.COMPLETED, AppointmentType.FOLLOWUP));

        // Doctor 6 has appointments, but none are completed.
        cm.addAppointment(new Appointment(12, 6, 108, 30, AppointmentStatus.SCHEDULED, AppointmentType.CONSULTATION));
        cm.addAppointment(new Appointment(13, 6, 109, 25, AppointmentStatus.NO_SHOW, AppointmentType.FOLLOWUP));

        Map<AppointmentType, Double> avg1 = cm.getAverageAppointmentDurationByType(1);
        assertEquals(2, avg1.size());
        assertTrue(avg1.containsKey(AppointmentType.CONSULTATION));
        assertEquals(127.0 / 3, avg1.get(AppointmentType.CONSULTATION), 1e-4);  // (30+41+56)/3
        assertTrue(avg1.containsKey(AppointmentType.FOLLOWUP));
        assertEquals(20.0, avg1.get(AppointmentType.FOLLOWUP), 1e-4);
        assertFalse(avg1.containsKey(AppointmentType.EMERGENCY));               // Only a cancelled emergency.

        Map<AppointmentType, Double> avg2 = cm.getAverageAppointmentDurationByType(2);
        assertEquals(1, avg2.size());
        assertTrue(avg2.containsKey(AppointmentType.EMERGENCY));
        assertEquals(40.0, avg2.get(AppointmentType.EMERGENCY), 1e-4);

        Map<AppointmentType, Double> avg4 = cm.getAverageAppointmentDurationByType(4);
        assertEquals(1, avg4.size());
        assertTrue(avg4.containsKey(AppointmentType.CONSULTATION));
        assertEquals(25.0, avg4.get(AppointmentType.CONSULTATION), 1e-4);

        Map<AppointmentType, Double> avg5 = cm.getAverageAppointmentDurationByType(5);
        assertEquals(1, avg5.size());
        assertTrue(avg5.containsKey(AppointmentType.FOLLOWUP));
        assertEquals(15.0, avg5.get(AppointmentType.FOLLOWUP), 1e-4);

        // Doctor with no appointments, and doctor with no completed appointments.
        assertTrue(cm.getAverageAppointmentDurationByType(3).isEmpty());
        assertTrue(cm.getAverageAppointmentDurationByType(6).isEmpty());

        // Unknown doctor.
        assertTrue(cm.getAverageAppointmentDurationByType(999).isEmpty());
    }
    
        // Add inside the existing TestSuite class:
    @Test
    public void testGetDoctorAppointmentSummary() {
        ClinicManager cm = new ClinicManager();
        cm.addDoctor(new Doctor(1, "dr_smith"));
        cm.addDoctor(new Doctor(2, "dr_jones"));
        cm.addDoctor(new Doctor(3, "dr_brown"));
        cm.addDoctor(new Doctor(4, "dr_lee"));

        // Appointments are added interleaved across doctors, in every status. All of them count.
        cm.addAppointment(new Appointment(1, 1, 100, 60, AppointmentStatus.COMPLETED, AppointmentType.CONSULTATION));
        cm.addAppointment(new Appointment(2, 2, 104, 50, AppointmentStatus.COMPLETED, AppointmentType.FOLLOWUP));
        cm.addAppointment(new Appointment(3, 1, 101, 10, AppointmentStatus.COMPLETED, AppointmentType.FOLLOWUP));
        cm.addAppointment(new Appointment(4, 4, 106, 25, AppointmentStatus.COMPLETED, AppointmentType.CONSULTATION));
        cm.addAppointment(new Appointment(5, 1, 102, 10, AppointmentStatus.CANCELLED, AppointmentType.FOLLOWUP));
        cm.addAppointment(new Appointment(6, 2, 105, 20, AppointmentStatus.COMPLETED, AppointmentType.EMERGENCY));
        cm.addAppointment(new Appointment(7, 1, 103, 15, AppointmentStatus.NO_SHOW, AppointmentType.FOLLOWUP));
        cm.addAppointment(new Appointment(8, 4, 107, 40, AppointmentStatus.COMPLETED, AppointmentType.FOLLOWUP));
        cm.addAppointment(new Appointment(9, 1, 108, 25, AppointmentStatus.SCHEDULED, AppointmentType.EMERGENCY));

        Map<Integer, DoctorAppointmentSummary> summary = cm.getDoctorAppointmentSummary();
        assertEquals(4, summary.size());

        // Doctor 1: five appointments in every status. FOLLOWUP has the most (3), although CONSULTATION has the most minutes.
        assertEquals(5, summary.get(1).totalAppointments);
        assertEquals(120, summary.get(1).totalMinutes);
        assertEquals(AppointmentType.FOLLOWUP, summary.get(1).busiestAppointmentType);

        // Doctor 2: FOLLOWUP and EMERGENCY are tied at one each, so EMERGENCY wins alphabetically.
        assertEquals(2, summary.get(2).totalAppointments);
        assertEquals(70, summary.get(2).totalMinutes);
        assertEquals(AppointmentType.EMERGENCY, summary.get(2).busiestAppointmentType);

        // Doctor 3: no appointments.
        assertEquals(0, summary.get(3).totalAppointments);
        assertEquals(0, summary.get(3).totalMinutes);
        assertNull(summary.get(3).busiestAppointmentType);

        // Doctor 4: CONSULTATION and FOLLOWUP are tied at one each, so CONSULTATION wins alphabetically.
        assertEquals(2, summary.get(4).totalAppointments);
        assertEquals(65, summary.get(4).totalMinutes);
        assertEquals(AppointmentType.CONSULTATION, summary.get(4).busiestAppointmentType);
    }
    
    }

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(TestSuite.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.getTrace());
        }
        if (result.wasSuccessful()) {
            System.out.println("All tests passed successfully.");
        } else {
            System.err.println("Tests failed: " + result.getFailureCount() + " test(s) failed.");
            System.exit(1);
        }
    }
}