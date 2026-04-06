package com.library.ui;

import com.library.model.Reservation;
import com.library.model.User;
import com.library.service.ReservationService;

import java.sql.SQLException;
import java.util.List;

public class ReservationStatusUI {

    private final ReservationService reservationService;
    private final User               user;

    public ReservationStatusUI(ReservationService reservationService, User user) {
        this.reservationService = reservationService;
        this.user               = user;
    }

    public void show() {
        try {
            List<Reservation> reservations = reservationService.getActiveReservations(user.getId());

            ConsoleUI.printHeader("VIEW RESERVATION STATUS");
            System.out.println();
            System.out.printf("  Member: %s [%s]%n", user.getDisplayName(), user.getMemberId());
            System.out.printf("  Active Reservations: %d%n%n", reservations.size());

            if (reservations.isEmpty()) {
                System.out.println("  You have no active reservations.");
            } else {
                System.out.println("  " + ConsoleUI.DASH_62);
                System.out.printf("  %-4s | %-28s | %-10s | %-6s | %-20s%n",
                    "#", "Title", "Res. Date", "Queue", "Status");
                System.out.println("  " + ConsoleUI.DASH_62);
                for (int i = 0; i < reservations.size(); i++) {
                    Reservation r = reservations.get(i);
                    System.out.printf("  %-4d | %-28s | %-10s | %-6s | %-20s%n",
                        i + 1,
                        ConsoleUI.col(r.getBookTitle(), 28),
                        r.getResDate(),
                        r.getQueueDisplay(),
                        r.getStatus());
                }
                System.out.println("  " + ConsoleUI.DASH_62);
            }

            System.out.println();
            System.out.println("  ACTIONS:");
            System.out.println("    [1] Cancel a Reservation");
            System.out.println("    [2] Back to Dashboard");
            System.out.println();

            String choice = ConsoleUI.readLine("  Enter choice: ");
            if (choice.equals("1")) cancelReservation(reservations);

        } catch (SQLException e) {
            ConsoleUI.error("Database error: " + e.getMessage());
            ConsoleUI.pressEnter();
        }
    }

    private void cancelReservation(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            ConsoleUI.error("No active reservations to cancel.");
            ConsoleUI.pressEnter();
            return;
        }
        System.out.println();
        String input = ConsoleUI.readLine("  Enter # of reservation to cancel: ");
        try {
            int idx = Integer.parseInt(input) - 1;
            if (idx < 0 || idx >= reservations.size()) {
                ConsoleUI.error("Invalid selection.");
                ConsoleUI.pressEnter();
                return;
            }
            Reservation r = reservations.get(idx);
            String confirm = ConsoleUI.readConfirm(
                "  Cancel reservation for \"" + r.getBookTitle() + "\"? [Y/N]: ");
            if (confirm.equals("N")) {
                ConsoleUI.info("Cancellation aborted.");
                ConsoleUI.pressEnter();
                return;
            }

            boolean ok = reservationService.cancelReservation(r.getId(), user.getId());
            if (ok) ConsoleUI.success("Reservation cancelled.");
            else    ConsoleUI.error("Failed to cancel reservation.");

        } catch (NumberFormatException e) {
            ConsoleUI.error("Invalid input.");
        } catch (SQLException e) {
            ConsoleUI.error("Database error: " + e.getMessage());
        }
        ConsoleUI.pressEnter();
    }
}
