import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.*;

public class UzumMarketBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "uzum_marketuzbot";
    }

    @Override
    public String getBotToken() {
        return "7887914075:AAFe0FqZJ1A5AdD_pu2LRJqP6x2j2_GgJOs";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }

    private void handleMessage(Message message) {
        String chatId = message.getChatId().toString();

        switch (message.getText()) {
            case "/start" -> sendMenu(chatId);
            case "📦 Mahsulotlar" -> sendProducts(chatId);
            case "🛒 Savatni ko‘rish" -> showCart(chatId);
            case "🗑 Savatni tozalash" -> clearCart(chatId); // ✅ yangi qo‘shildi
            case "🖼 Rasm yuborish" -> sendPhoto(chatId);
            case "📁 Fayl yuborish" -> sendFile(chatId);
            default -> sendMessage(chatId, "Iltimos, menyudan tanlang.");
        }
    }

    private void sendMenu(String chatId) {
        SendMessage message = new SendMessage(chatId, "Uzum Market botiga xush kelibsiz!");
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("📦 Mahsulotlar");
        row1.add("🛒 Savatni ko‘rish");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🗑 Savatni tozalash");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("🖼 Rasm yuborish");
        row3.add("📁 Fayl yuborish");

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        markup.setKeyboard(rows);
        markup.setResizeKeyboard(true);
        message.setReplyMarkup(markup);

        executeSafe(message);
    }

    private void sendProducts(String chatId) {
        SendMessage message = new SendMessage(chatId, "Mahsulotlarni tanlang:");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Product p : Storage.products) {
            InlineKeyboardButton btn = new InlineKeyboardButton(p.getName() + " - " + p.getPrice() + " so'm");
            btn.setCallbackData("add_to_cart:" + p.getId());
            rows.add(List.of(btn));
        }
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);
        executeSafe(message);
    }

    private void showCart(String chatId) {
        List<Product> cartItems = Storage.cart.getOrDefault(chatId, new ArrayList<>());

        if (cartItems.isEmpty()) {
            sendMessage(chatId, "Savat bo‘sh.");
            return;
        }

        StringBuilder sb = new StringBuilder("🛒 Sizning savatingiz:\n\n");
        int total = 0;

        for (Product p : cartItems) {
            sb.append("- ").append(p.getName()).append(" — ").append(p.getPrice()).append(" so'm\n");
            total += p.getPrice();
        }

        sb.append("\nJami: ").append(total).append(" so'm");
        sendMessage(chatId, sb.toString());
    }

    private void clearCart(String chatId) {
        Storage.cart.remove(chatId);
        sendMessage(chatId, "✅ Savatingiz tozalandi.");
    }

    private void sendPhoto(String chatId) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setCaption("Mana bu bizning mahsulot rasmimiz:");
        photo.setPhoto(new InputFile(new File("src/main/resources/apple.jpg")));
        executeSafe(photo);
    }

    private void sendFile(String chatId) {
        SendDocument doc = new SendDocument();
        doc.setChatId(chatId);
        doc.setCaption("Mana fayl:");
        doc.setDocument(new InputFile(new File("src/main/resources/catalog.pdf")));
        executeSafe(doc);
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        String chatId = callbackQuery.getMessage().getChatId().toString();

        if (data.startsWith("add_to_cart:")) {
            String productId = data.split(":")[1];
            Product selected = Storage.products.stream()
                    .filter(p -> p.getId().equals(productId))
                    .findFirst().orElse(null);

            if (selected != null) {
                Storage.cart.computeIfAbsent(chatId, k -> new ArrayList<>()).add(selected);
                sendMessage(chatId, selected.getName() + " savatga qo‘shildi!");
            }
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        executeSafe(message);
    }

    private void executeSafe(BotApiMethod<?> method) {
        try {
            execute(method);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void executeSafe(SendPhoto photo) {
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void executeSafe(SendDocument doc) {
        try {
            execute(doc);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
