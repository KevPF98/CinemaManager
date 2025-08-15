package com.cinemamanager.model.sale;
import com.cinemamanager.iface.Identifiable;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class Invoice implements Identifiable <Integer> {

    private final int invoiceNumber;
    private List <Ticket> tickets;
    private final String customerId;
    private double total;
    private final LocalDate issueDate;

    public Invoice (int invoiceNumber, List<Ticket> tickets, String customerId) {
        this.invoiceNumber = invoiceNumber;
        this.tickets = tickets;
        this.customerId = customerId;
        this.total = calculateTotal();
        this.issueDate = LocalDate.now();
    }

    public Integer getId () {
        return invoiceNumber;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List <Ticket> tickets) {
        this.tickets = tickets;
        this.total = calculateTotal();
    }

    public String getCustomerId() {
        return customerId;
    }

    public double getTotal() {
        return total;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    private double calculateTotal() {
        return tickets.stream()
                .mapToDouble(t -> t.getShowtime().getPrice())
                .sum();
    }

    public void applyDiscount (double factor) {
        this.total *= factor;
    }

    public void printInvoice() {
        System.out.println("------------------------------------------------------------");
        System.out.println("INVOICE #: " + invoiceNumber + "       | DATE: " + issueDate);
        System.out.println("------------------------------------------------------------");
        System.out.println("MOVIE                          | PRICE");
        tickets.forEach(ticket -> System.out.println(
                ticket.getShowtime().getMovie().getTitle() +
                        " | $" + ticket.getShowtime().getPrice()
        ));
        System.out.println("------------------------------------------------------------");
        System.out.println("TOTAL: $" + total);
        System.out.println("CUSTOMER ID: " + customerId);
        System.out.println("------------------------------------------------------------");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return invoiceNumber == invoice.invoiceNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(invoiceNumber);
    }

    @Override
    public String toString() {
        return  "Invoice number: " + invoiceNumber + "\n" +
                "Tickets: " + tickets.size() + "\n" +
                "Customer National ID: " + customerId + "\n" +
                "Total: " + total + "\n" +
                "Issue date: " + issueDate + "\n" +
                "\n-----------------\n";
    }

}
