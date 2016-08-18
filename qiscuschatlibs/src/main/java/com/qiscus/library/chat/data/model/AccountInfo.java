package com.qiscus.library.chat.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class AccountInfo implements Parcelable {
    private int id;
    private String email;
    private String fullname;
    private Date dateOfBirth;
    private int age;
    private String image;
    private String gender;
    private String authenticationToken;
    private String userChannel;
    private double balance;
    private String city;
    private double latitude;
    private double longitude;
    private boolean firstLogin;
    private boolean verified;
    private String phoneNumber;

    public AccountInfo() {
        dateOfBirth = new Date();
    }

    protected AccountInfo(Parcel in) {
        id = in.readInt();
        email = in.readString();
        fullname = in.readString();
        dateOfBirth = new Date(in.readLong());
        age = in.readInt();
        image = in.readString();
        gender = in.readString();
        authenticationToken = in.readString();
        userChannel = in.readString();
        balance = in.readDouble();
        city = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        firstLogin = in.readByte() != 0;
        verified = in.readByte() != 0;
        phoneNumber = in.readString();
    }

    public static final Creator<AccountInfo> CREATOR = new Creator<AccountInfo>() {
        @Override
        public AccountInfo createFromParcel(Parcel in) {
            return new AccountInfo(in);
        }

        @Override
        public AccountInfo[] newArray(int size) {
            return new AccountInfo[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public String getUserChannel() {
        return userChannel;
    }

    public void setUserChannel(String userChannel) {
        this.userChannel = userChannel;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(email);
        dest.writeString(fullname);
        dest.writeLong(dateOfBirth.getTime());
        dest.writeInt(age);
        dest.writeString(image);
        dest.writeString(gender);
        dest.writeString(authenticationToken);
        dest.writeString(userChannel);
        dest.writeDouble(balance);
        dest.writeString(city);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeByte((byte) (firstLogin ? 1 : 0));
        dest.writeByte((byte) (verified ? 1 : 0));
        dest.writeString(phoneNumber);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AccountInfo && id == ((AccountInfo) o).id;
    }

    @Override
    public String toString() {
        return "AccountInfo{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullname='" + fullname + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", age=" + age +
                ", image='" + image + '\'' +
                ", gender='" + gender + '\'' +
                ", authenticationToken='" + authenticationToken + '\'' +
                ", userChannel='" + userChannel + '\'' +
                ", balance=" + balance +
                ", city='" + city + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", firstLogin=" + firstLogin +
                ", verified=" + verified +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
