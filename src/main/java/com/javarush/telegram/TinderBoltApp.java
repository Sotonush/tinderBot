package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "sotonutinderai_bot";
    public static final String TELEGRAM_BOT_TOKEN = "7513189600:AAEwhFbrs7BljUygjMS-J4hQYIW3qrUbWpo";
    public static final String OPEN_AI_TOKEN = "gpt:FUj5RRFaxyJPx8r99jGlJFkblB3TiwFfStVTSk3zPf89Bss9";


    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    private DialogMode currentMode = null;
    private ArrayList<String> list = new ArrayList<>();

    private UserInfo me;
    private UserInfo she;
    private int queshionCount;

    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();

        if(message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String textMain = loadMessage("main");
            sendTextMessage(textMain);

            showMainMenu("Начало", "/start",
                    "генерация Tinder-профля \uD83D\uDE0E", "/profile",
                    "сообщение для знакомства \uD83E\uDD70", "/opener",
                    "переписка от вашего имени \uD83D\uDE08", "/message",
                    "переписка со звездами \uD83D\uDD25", "/date",
                    "задать вопрос чату GPT \uD83E\uDDE0","/gpt");
            return;
        }
        if(message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String textGpt = loadMessage("gpt");
            sendTextMessage(textGpt);
            return;
        }
        if(currentMode == DialogMode.GPT && !isMessageCommand()) {
            String promt = loadPrompt("gpt");
            Message msg = sendTextMessage("Подождите пару секунд - чат думает");
            String answer  = chatGPT.sendMessage(promt, message);
            updateTextMessage(msg, answer);
            return;
        }
        if(message.equals("/date")) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String textDate = loadMessage("date");
            sendTextButtonsMessage(textDate,
                    "Ариана гранде", "date_grande",
                    "Gosling", "date_gosling",
                    "Hardy","date_hardy",
                    "Robbie","date_robbie");
            return;
        }
        if (currentMode == DialogMode.DATE && !isMessageCommand()) {
            String query = getCallbackQueryButtonKey();
            if(query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage("Good!");
                String promt = loadPrompt(query);
                chatGPT.setPrompt(promt);
                return;
            }
            String answer = chatGPT.addMessage(message);
            sendTextMessage(answer);
            return;
        }

        if(message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Prishli",
                    "Следуещее сообщение", "message_next",
                    "Пригласить на свидание","message_date");
            return;
        }
        if (currentMode == DialogMode.MESSAGE && !isMessageCommand()) {
            String query = getCallbackQueryButtonKey();
            if(query.startsWith("message_")) {
                String promt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);

                Message msg = sendTextMessage("Подождите пару секунд - чат думает");
                String answer = chatGPT.sendMessage(promt,userChatHistory);
                updateTextMessage(msg, answer);
            }
            list.add(message);
            return;
        }

        if(message.equals("/profile")) {
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");

            me = new UserInfo();
            queshionCount = 1;
            sendTextMessage("Сколько вам лет?");

            return;
        }
        if(currentMode == DialogMode.PROFILE && !isMessageCommand()) {
            switch (queshionCount) {
                case 1:
                    me.age = message;
                    queshionCount = 2;
                    sendTextMessage("Кем вы работаете");
                    return;
                case 2:
                    me.occupation = message;

                    queshionCount = 3;
                    sendTextMessage("Ваше хобби");
                    return;
                case 3:
                    me.hobby = message;
                    String aboutMyself = me.toString();
                    String promt = loadPrompt("profile");
                    Message msg = sendTextMessage("Подождите пару секунд - чат думает");
                    String answer = chatGPT.sendMessage(promt, aboutMyself);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }
        if (message.equals("/opener")) {
            currentMode = DialogMode.OPENER;
            sendPhotoMessage("opener");

            she = new UserInfo();
            queshionCount = 1;
            sendTextMessage("Имя девушки");
            return;
        }
        if (currentMode == DialogMode.OPENER && !isMessageCommand()) {
            switch (queshionCount) {
                case 1:
                    she.name = message;
                    queshionCount = 2;
                    sendTextMessage("Сколько ей лет");
                    return;
                case 2:
                    she.age = message;
                    queshionCount = 3;
                    sendTextMessage("Какие у нее хобби");
                    return;
                case 3:
                    she.hobby = message;
                    queshionCount = 4;
                    sendTextMessage("Кем она работает");
                    return;
                case 4:
                    she.occupation = message;
                    queshionCount = 5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    she.goals = message;
                    String aboutFriend = message;
                    String promt = loadPrompt("opener");

                    Message msg = sendTextMessage("Подождите пару секунд чат думает");
                    String answer = chatGPT.sendMessage(promt, aboutFriend);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }
    }


    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
