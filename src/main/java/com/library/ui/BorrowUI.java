package com.library.ui;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.User;
import com.library.service.BookService;
import com.library.service.LoanService;

import java.sql.SQLException;
import java.time.LocalDate;

public class BorrowUI {

    private final LoanService loanService;
    private final BookService bookService;
    private final User        user;

    public BorrowUI(LoanService loanService, BookService bookService, User user) {
        this.loanService = loanService;
        this.bookService = bookService;
        this.user        = user;
    }

    public void show() {
        ConsoleUI.printHeader("BORROW BOOKS");
        System.out.println();
        String bookIdInput = ConsoleUI.readLine("  Enter Book ID (e.g. BOOK-0001): ");
        try {
            Book book = bookService.findByBookId(bookIdInput);
            if (book == null) {
                ConsoleUI.error("Book not found with ID: " + bookIdInput);
                ConsoleUI.pressEnter();
                return;
            }
            borrowByBook(book);
        } catch (SQLException e) {
            ConsoleUI.error("Database error: " + e.getMessage());
            ConsoleUI.pressEnter();
        }
    }

    public void borrowByBook(Book book) throws SQLException {
        System.out.println();
        System.out.printf("  Book Found: %s%n", book.getTitle());
        System.out.printf("  Available Copies : %d%n", book.getAvailableCopies());

        if (book.getAvailableCopies() <= 0) {
            ConsoleUI.error("No copies available. Please reserve this book.");
            ConsoleUI.pressEnter();
            return;
        }

        String dueDate = LocalDate.now().plusDays(14).toString();
        System.out.printf("  Due Date         : %s%n%n", dueDate);

        String confirm = ConsoleUI.readConfirm("  Confirm borrow? [Y/N]: ");
        if (confirm.equals("N")) {
            ConsoleUI.info("Borrow cancelled.");
            ConsoleUI.pressEnter();
            return;
        }

        System.out.println("\n  Processing checkout...");

        LoanService.BorrowResult result = loanService.borrowBook(user.getId(), book.getBookId());

        switch (result) {
            case SUCCESS -> {
                Loan loan = loanService.getLastLoan(user.getId(), book.getBookId());
                ConsoleUI.success("Book checked out!");
                if (loan != null) {
                    System.out.printf("  Due Date  : %s%n", loan.getDueDate());
                    System.out.printf("  Loan ID   : %s%n", loan.getLoanId());
                }
            }
            case NO_COPIES         -> ConsoleUI.error("No copies available.");
            case MAX_LOANS_REACHED -> ConsoleUI.error("You have reached the maximum of 5 active loans.");
            case ALREADY_BORROWED  -> ConsoleUI.error("You already have an active loan for this book.");
            default                -> ConsoleUI.error("An error occurred.");
        }
        ConsoleUI.pressEnter();
    }
}

