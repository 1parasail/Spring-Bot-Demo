package io.project.demospringsebot.config;


import io.project.demospringsebot.service.TgBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
public class BotInit {

    @Autowired
    TgBot tgBot;

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException
    {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(tgBot);
        }
        catch (TelegramApiException e)
        {
            log.error("Error registering bot: z" + e.getMessage());
        }
    }
}
