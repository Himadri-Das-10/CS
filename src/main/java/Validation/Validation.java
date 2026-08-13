package Validation;

import CODES.CODES;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Validation
{
    private static Validation validation;

    public static final String errorTemplateInvalidCSVdata = "Invalid CSV data. Make sure these are the headers. name,age,class,division,sex,seating_preference. The divisions are from A-D and grades are (Primary School, Middle School, Secondary School, Higher Secondary School). For null values (except name), put 'EMPTY'";
    public static final String errorTemplateInvalidSeats = "Please add a whole number of seats and make sure the number of seats is greater than or equal to the number of seats";

    private Validation(){}
    public boolean isValidEmail(String email) {

        return email.matches(
                "^[A-Za-z0-9_.]+@[A-Za-z]+\\.[A-Za-z]{2,}$"
        );
    }










    public CODES validateCSV(File csvFile)
    {
        String expectedHeader =
                "name,age,class,division,sex,seating_preference,cannot_sit_with,image_path";

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(csvFile))) {
            // Check whether the CSV is empty.
            String header = reader.readLine();

            if (header == null || header.isBlank()) {
                System.out.println(CODES.INVALID + "CSV empty");
                return CODES.INVALID;
            }

            // Check whether the header is correct.
            if (!header.strip().equalsIgnoreCase(expectedHeader)) {
                System.out.println(CODES.INVALID + "Header incorrect");
                return CODES.INVALID;
            }

            String line;

            /*
             * Store all student names.
             * This allows cannot_sit_with to reference students
             * regardless of where they appear in the CSV.
             */
            List<String> studentNames = new ArrayList<>();

            /*
             * Store each student's cannot_sit_with values.
             * The validation is performed after reading all students.
             */
            Map<String, List<String>> cannotSitWithMap = new HashMap<>();

            // Validate every student row.
            String imgPath = null;
            while ((line = reader.readLine()) != null) {
                // Ignore blank lines.
                if (line.isBlank()) {
                    continue;
                }

                // -1 preserves empty fields at the end of the row.
                String[] data = line.split(",", -1);

                // Every row must contain exactly 8 fields.
                if (data.length != 8) {
                    System.out.println(CODES.INVALID + "Length");
                    return CODES.INVALID;
                }

                String name = data[0].strip();
                String age = data[1].strip();
                String classLevel = data[2].strip();
                String division = data[3].strip();
                String sex = data[4].strip();
                String seatingPreference = data[5].strip();
                String cannotSitWithData = data[6].strip();
                imgPath = data[7].strip();

                /*
                 * NAME
                 */
                if (name.isEmpty()) {
                    System.out.println(CODES.INVALID + "Name is empty");
                    return CODES.INVALID;
                }

//                // Check for duplicate student names.
//                if (studentNames.contains(name))
//                {
//                    System.out.println(CODES.INVALID);
//                    return CODES.INVALID;
//                }

                studentNames.add(name);

                /*
                 * AGE
                 */
                if (!age.equals(String.valueOf(CODES.EMPTY))) {
                    try {
                        Integer.parseInt(age);
                    } catch (NumberFormatException e) {
                        System.out.println(CODES.INVALID + "Age isnt a number");
                        return CODES.INVALID;
                    }
                }

                /*
                 * CLASS
                 */
                if (!classLevel.equals("Primary School") &&
                        !classLevel.equals("Middle School") &&
                        !classLevel.equals("Secondary School") &&
                        !classLevel.equals("Higher Secondary School") &&
                        !classLevel.equals(String.valueOf(CODES.EMPTY))) {
                    System.out.println(CODES.INVALID + "Problem with class");
                    return CODES.INVALID;
                }

                /*
                 * DIVISION
                 */
                if (!division.equalsIgnoreCase("A") &&
                        !division.equalsIgnoreCase("B") &&
                        !division.equalsIgnoreCase("C") &&
                        !division.equalsIgnoreCase("D") &&
                        !division.equalsIgnoreCase(
                                String.valueOf(CODES.EMPTY))) {
                    System.out.println(CODES.INVALID + "Problem with division");
                    return CODES.INVALID;
                }

                /*
                 * SEX
                 */
                if (!sex.equalsIgnoreCase("Male") &&
                        !sex.equalsIgnoreCase("Female") &&
                        !sex.equalsIgnoreCase(
                                String.valueOf(CODES.EMPTY))) {
                    System.out.println(CODES.INVALID + "Problem with sex");
                    return CODES.INVALID;
                }

                /*
                 * SEATING PREFERENCE
                 */
                if (!seatingPreference.equalsIgnoreCase("Front") &&
                        !seatingPreference.equalsIgnoreCase("Back") &&
                        !seatingPreference.equalsIgnoreCase(
                                String.valueOf(CODES.EMPTY))) {
                    System.out.println(CODES.INVALID + "Problem with seating pref");
                    return CODES.INVALID;
                }

                /*
                 * CANNOT SIT WITH
                 *
                 * Multiple students are separated using ;
                 *
                 * Example:
                 * Rahul;Arjun;Sarah
                 */
                List<String> cannotSitWith = new ArrayList<>();

                if (!cannotSitWithData.isEmpty() &&
                        !cannotSitWithData.equalsIgnoreCase(String.valueOf(CODES.EMPTY))) {
                    String[] restrictedStudents =
                            cannotSitWithData.split(";", -1);

                    for (String restrictedStudent :
                            restrictedStudents) {
                        restrictedStudent = restrictedStudent.strip();

                        // Empty name inside the list is invalid.
                        if (restrictedStudent.isEmpty()) {
                            System.out.println(CODES.INVALID + "Problem with cannot sit | empty name in list");
                            return CODES.INVALID;
                        }

                        // Student cannot be restricted from themselves.
                        if (restrictedStudent.equalsIgnoreCase(name)) {
                            System.out.println(CODES.INVALID+ "Problem with seating pref | student cannot sit with himself");
                            return CODES.INVALID;
                        }

                        // Duplicate restricted student.
                        String finalRestrictedStudent = restrictedStudent;
                        if (cannotSitWith.stream()
                                .anyMatch(s ->
                                        s.equalsIgnoreCase(
                                                finalRestrictedStudent))) {
                            System.out.println(CODES.INVALID + "Problem with seating pref | Duplicate restricted student. ");
                            return CODES.INVALID;
                        }

                        cannotSitWith.add(restrictedStudent);
                    }
                }

                cannotSitWithMap.put(name, cannotSitWith);
            }

            /*
             * CANNOT SIT WITH — CHECK STUDENT NAMES
             *
             * Now that every student has been read, check that
             * every restricted student actually exists in the CSV.
             */
            for (Map.Entry<String, List<String>> entry :
                    cannotSitWithMap.entrySet()) {
                for (String restrictedStudent : entry.getValue()) {
                    boolean exists = studentNames.stream()
                            .anyMatch(name ->
                                    name.equalsIgnoreCase(
                                            restrictedStudent));

                    if (!exists) {
                        System.out.println(CODES.INVALID + "cannot sit with student does not exist in CSV");
                        return CODES.INVALID;
                    }
                }

            }




                // CSV passed every validation.
                return CODES.SUCCESS;
        }
        catch (IOException e)
        {
            e.printStackTrace();

            return CODES.INVALID;
        }
    }





    public CODES validateIntegerField(Pane errorPane, String value1, Text errorText, String typeOfErrorToLook, String value2)
    {
        try{
            int num1 = Integer.parseInt(value1);
            int num2 = Integer.parseInt(value2);

            switch (typeOfErrorToLook){
                case "SEATS":
                    if(value1.isEmpty() || value2.isEmpty())
                        return CODES.INVALID;

                    if(num1 < num2)
                        return CODES.INVALID;


            }
            return CODES.SUCCESS;
        }
        catch (NumberFormatException e)
        {

            return CODES.INVALID;
        }

    }



    //Getters
    public static Validation getInstance() {
        if (validation == null) {
            validation = new Validation();
        }
        return validation;
    }
}
