# UI Customization

### Theme Customization

Boring with default template? You can customized it, try it!, we have more items than those below code, its just example.

```java
Qiscus.getChatConfig()
      .setStatusBarColor(R.color.blue)
      .setAppBarColor(R.color.red)
      .setTitleColor(R.color.white)
      .setLeftBubbleColor(R.color.green)
      .setRightBubbleColor(R.color.yellow)
      .setRightBubbleTextColor(R.color.white)
      .setRightBubbleTimeColor(R.color.grey)
      .setTimeFormat(date -> new SimpleDateFormat("HH:mm").format(date));
```



### UI Source code

If you want full customisations, you can modify everything on the view by forking our repository or just right away modifying our **[CustomChatActivity.java](https://github.com/qiscus/qiscus-sdk-android/blob/develop/app/src/main/java/com/qiscus/dragonfly/CustomChatActivity.java) **based on your needs.