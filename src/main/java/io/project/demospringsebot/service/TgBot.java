package io.project.demospringsebot.service;


import io.project.demospringsebot.config.BotConfig;
import io.project.demospringsebot.model.User;
import io.project.demospringsebot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TgBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    final BotConfig config;

    final static String HELP_TEXT = "This is a demo bot, which was created with Java/Spring \n\n" +
                                     "You can execute commands from the main menu or type it manually\n\n" +
                                     "Type /start to begin";

    public TgBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList(); //Initialization of Menu
        listOfCommands.add(new BotCommand("/start", "Starts the bot"));
        listOfCommands.add(new BotCommand("/mydata", "Data of user"));
        listOfCommands.add(new BotCommand("/deletedata", "Delete data of user"));
        listOfCommands.add(new BotCommand("/help", "Guide, how to use the bot"));
        listOfCommands.add(new BotCommand("/setting", "Set your preferences"));
        try
        {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting up commands: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() == true && update.getMessage().hasText() == true) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (message) {
                case "/start":
                    try {
                        registerUser(update.getMessage());
                        startCommand(chatId, update.getMessage().getChat().getFirstName());
                        break;
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    sendMessage(chatId, "Invalid command");
            }
        }
    }

    private void startCommand(long chatId, String name) throws TelegramApiException {
        String answer = "Hi, " + name + "!";
        log.info("Result of starting command: " + answer);

        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRow = new KeyboardRow();

        keyboardRow.add("text");
        keyboardRow.add("text1");

        keyboardRows.add(keyboardRow);

        KeyboardRow keyboardRow1 = new KeyboardRow();

        keyboardRow1.add("text2");
        keyboardRow1.add("text3");
        keyboardRow1.add("text4");

        keyboardRows.add(keyboardRow1);

        keyboardMarkup.setKeyboard(keyboardRows);

        sendMessage.setReplyMarkup(keyboardMarkup);




        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    private void registerUser(Message message) throws TelegramApiException {

        if (userRepository.findById(message.getChatId()).isEmpty())
        {
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisterAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);

            log.info("Registered user: " + user);
        }
    }

}

