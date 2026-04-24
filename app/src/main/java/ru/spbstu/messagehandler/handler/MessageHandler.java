package ru.spbstu.messagehandler.handler;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import ru.spbstu.messagehandler.service.TelegramCommandRouter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.spbstu.messagehandler.service.UserService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class MessageHandler {

    public static final Pattern FORM_LINK_REGEX = Pattern.compile(
            "^(https?:\\/\\/)?(www\\.)?(docs\\.google\\.com\\/(u\\/\\d\\/)?" +
                    "forms\\/d\\/(e\\/)?[a-zA-Z0-9_-]+(\\/viewform)?|forms\\.gle\\/" +
                    "[a-zA-Z0-9_-]+)(\\?.*)?$"
    );
    private final TelegramCommandRouter telegramCommandRouter;
    private final UserService userService;

    public MessageHandler(TelegramCommandRouter telegramCommandRouter, UserService userService) {
        this.telegramCommandRouter = telegramCommandRouter;
        this.userService = userService;
    }


    public SendMessage handle(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();
        String firstName = message.getFrom().getFirstName();
        userService.getOrCreateUser(chatId, firstName);

        String responseText;

        switch (text) {
            case "/help":
                responseText = telegramCommandRouter.handleHelp();
                break;
            case "/myforms":
                responseText = telegramCommandRouter.handleMyForms(chatId);
                break;
            case String s when s.startsWith("/solve"):
                responseText = telegramCommandRouter.handleSolve(text.replaceFirst("^/solve", ""),
                    chatId);
                break;
            case String s when s.startsWith("/rescore"):
                responseText = telegramCommandRouter.handleRescore(text.replaceFirst("^/rescore",
                    ""), chatId);
                break;
            case String s when s.startsWith("/history"):
                responseText = telegramCommandRouter.handleHistory(text.replaceFirst("^/history",
                    ""), chatId);
                break;
            case String s when s.startsWith("/get_form"):
                responseText = telegramCommandRouter.handleGetForm(text.replaceFirst("^/get_form",
                    ""), chatId);
                break;
            case String s when s.startsWith("/remove_form"):
                responseText = telegramCommandRouter.handleRemoveForm(text.replaceFirst(
                        "^/remove_form", ""), chatId);
                break;
            case String s when s.startsWith("/status"):
                responseText = telegramCommandRouter.handleStatus(text.replaceFirst("^/status",
                    ""), chatId);
                break;
            default:
                Matcher matcher = FORM_LINK_REGEX.matcher(text);
                if (matcher.find()) {
                    responseText = telegramCommandRouter.handleSolve(matcher.group(), chatId);
                } else {
                    responseText = telegramCommandRouter.handleUnknownCommand();
                }

        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(responseText);
        sendMessage.setParseMode(ParseMode.HTML);
        return sendMessage;
    }
}
