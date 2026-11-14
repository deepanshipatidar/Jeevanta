package com.hackathon.admin;

import com.hackathon.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Admin Dashboard — Hackathon Hospital Bed Management Console
 *
 * A standalone JDBC console application that connects to the MySQL
 * database and lets an admin view / update hospital bed counts.
 */
public class AdminConsole {

    // ─── SQL Statements ────────────────────────────────────────────────
    private static final String SQL_SELECT_ALL =
            "SELECT id, name, available_beds FROM hospitals ORDER BY id";

    private static final String SQL_UPDATE_BEDS =
            "UPDATE hospitals SET available_beds = ? WHERE id = ?";

    // ─── Entry Point ───────────────────────────────────────────────────
    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║     🏥  HACKATHON ADMIN DASHBOARD  🏥       ║");
        System.out.println("║        Hospital Bed Management Console       ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();

        try (Connection connection = DBUtil.getConnection();
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("[✓] Connected to database: hackathon_db");
            System.out.println();

            boolean running = true;

            while (running) {
                printMenu();
                System.out.print("Enter your choice: ");
                String input = scanner.nextLine().trim();

                switch (input) {
                    case "1":
                        viewAllHospitals(connection);
                        break;
                    case "2":
                        updateBedCount(connection, scanner);
                        break;
                    case "3":
                        running = false;
                        System.out.println("\n[✓] Exiting Admin Dashboard. Goodbye!\n");
                        break;
                    default:
                        System.out.println("\n[✗] Invalid choice. Please enter 1, 2, or 3.\n");
                        break;
                }
            }

        } catch (SQLException e) {
            System.err.println("\n[✗] FATAL — Could not connect to the database.");
            System.err.println("    Reason : " + e.getMessage());
            System.err.println("    SQL State: " + e.getSQLState());
            System.err.println("\n    Make sure MySQL is running and hackathon_db exists.\n");
        }
    }

    // ─── Menu ──────────────────────────────────────────────────────────
    private static void printMenu() {
        System.out.println("┌──────────────────────────────────────┐");
        System.out.println("│            MAIN  MENU                │");
        System.out.println("├──────────────────────────────────────┤");
        System.out.println("│  1. View all hospitals & bed counts  │");
        System.out.println("│  2. Update a hospital's bed count    │");
        System.out.println("│  3. Exit                             │");
        System.out.println("└──────────────────────────────────────┘");
    }

    // ─── Option 1: View All Hospitals ──────────────────────────────────
    private static void viewAllHospitals(Connection connection) {
        System.out.println();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SELECT_ALL)) {

            System.out.println("┌──────┬────────────────────────────────────┬────────────────┐");
            System.out.println("│  ID  │ Hospital Name                      │ Available Beds │");
            System.out.println("├──────┼────────────────────────────────────┼────────────────┤");

            boolean hasRows = false;

            while (rs.next()) {
                hasRows = true;
                int    id   = rs.getInt("id");
                String name = rs.getString("name");
                int    beds = rs.getInt("available_beds");

                System.out.printf("│ %-4d │ %-34s │ %-14d │%n", id, name, beds);
            }

            if (!hasRows) {
                System.out.println("│              (no hospitals found)                        │");
            }

            System.out.println("└──────┴────────────────────────────────────┴────────────────┘");

        } catch (SQLException e) {
            System.err.println("[✗] Error fetching hospitals: " + e.getMessage());
        }

        System.out.println();
    }

    // ─── Option 2: Update Bed Count ────────────────────────────────────
    private static void updateBedCount(Connection connection, Scanner scanner) {
        System.out.println();

        // --- Read Hospital ID ---
        System.out.print("Enter Hospital ID to update: ");
        String idInput = scanner.nextLine().trim();
        int hospitalId;

        try {
            hospitalId = Integer.parseInt(idInput);
        } catch (NumberFormatException e) {
            System.out.println("[✗] Invalid ID. Please enter a numeric value.\n");
            return;
        }

        // --- Read New Bed Count ---
        System.out.print("Enter new available bed count: ");
        String bedInput = scanner.nextLine().trim();
        int newBedCount;

        try {
            newBedCount = Integer.parseInt(bedInput);
        } catch (NumberFormatException e) {
            System.out.println("[✗] Invalid bed count. Please enter a numeric value.\n");
            return;
        }

        if (newBedCount < 0) {
            System.out.println("[✗] Bed count cannot be negative.\n");
            return;
        }

        // --- Execute Update ---
        try (PreparedStatement pstmt = connection.prepareStatement(SQL_UPDATE_BEDS)) {
            pstmt.setInt(1, newBedCount);
            pstmt.setInt(2, hospitalId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.printf("[✓] Success! Hospital ID %d now has %d available bed(s).%n%n",
                        hospitalId, newBedCount);
            } else {
                System.out.printf("[✗] No hospital found with ID %d. No rows updated.%n%n",
                        hospitalId);
            }

        } catch (SQLException e) {
            System.err.println("[✗] Error updating bed count: " + e.getMessage());
            System.out.println();
        }
    }
}
