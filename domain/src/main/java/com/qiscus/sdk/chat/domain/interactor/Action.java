package com.qiscus.sdk.chat.domain.interactor;

/**
 * Created on : September 26, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public interface Action<T> {
    void call(T t);
}
