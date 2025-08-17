package com.cinemamanager.manager.sale;
import com.cinemamanager.manager.user.UserManager;
import com.cinemamanager.model.people.User;
import com.cinemamanager.model.sale.Invoice;
import com.cinemamanager.model.sale.Ticket;
import com.cinemamanager.util.common.ConsoleUtil;
import com.cinemamanager.util.common.JsonUtil;
import com.cinemamanager.util.common.StorageManager;
import com.cinemamanager.util.common.enums.CollectionType;
import com.cinemamanager.util.common.exception.DuplicateElementException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public final class InvoiceManager {
    private final StorageManager <Integer, Invoice> invoiceStorageManager;
    private final static String INVOICE_FILE_PATH = "invoices.json";
    private int nextId;

    private final TicketManager ticketManager;
    private final UserManager userManager;

    public InvoiceManager (TicketManager ticketManager, UserManager userManager) {
        this.invoiceStorageManager = new StorageManager<> (CollectionType.ARRAY_LIST);
        this.ticketManager = ticketManager;
        this.userManager = userManager;

        loadFromFile();

        OptionalInt maxId = invoiceStorageManager.findAll().stream()
                .mapToInt(Invoice::getId)
                .max();
        this.nextId = maxId.isPresent() ? maxId.getAsInt() +1 : 1;
    }

    public Optional <Invoice> addInvoice () { // Llamar al showtime.saveToFile despues de este metodo
        User user;
        while (true) {
            Optional <User> optionalUser = userManager.getOrCreateUser();
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
                break;
            }
            if (!ConsoleUtil.confirm("\nDo you want to try with another National ID?")) return Optional.empty();
        }
        List <Ticket> tickets = ticketManager.createTickets();
        if (tickets.isEmpty()) return Optional.empty();
        Invoice invoice = new Invoice (nextId++, tickets, user.getPersonalData().getId());
        if (ConsoleUtil.confirm("\nAmount to pay: $" + invoice.getTotal() + ".\nDo you want to proceed?")) {
            try {
                invoiceStorageManager.add(invoice, false);
                ticketManager.saveToFile();
                saveToFile();
                System.out.println("\nInvoice generated successfully!\n");
                return Optional.of(invoice);
            } catch (DuplicateElementException e) {
                System.out.println("\nError adding the invoice: " + e.getMessage());
            }
        }
        ticketManager.getAllTickets().removeAll(tickets);
        return Optional.empty();
    }

    private void loadFromFile () {
        Type type = new TypeToken <List <Invoice>>() {}.getType();
        List <Invoice> loaded = JsonUtil.read (INVOICE_FILE_PATH, type, ArrayList::new);
        invoiceStorageManager.clear();
        for (Invoice invoice : loaded) {
            try {
                invoiceStorageManager.add(invoice, true);
            } catch (DuplicateElementException ignored) {}
        }
    }

    private void saveToFile () {
        List <Invoice> list = new ArrayList<> (invoiceStorageManager.findAll());
        JsonUtil.write(INVOICE_FILE_PATH, list);
    }

}
