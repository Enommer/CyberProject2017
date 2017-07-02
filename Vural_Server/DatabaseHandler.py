"""Created by Emanuel Amit 5/30/2017 2:49"""
import sqlite3
from time import time


class DatabaseHandler:

    def __init__(self):
        self.DB = sqlite3.connect("vural_database.db")

        if not self.DB.execute("SELECT * FROM counters").fetchone():
            self.DB.execute("INSERT INTO counters VALUES (0, 0)")

        self.DB.commit()

    """ CREATE ROW """

    def create_user(self, user_id, phone_number, username):
        self.DB.execute("INSERT INTO users VALUES (?, ?, ?, ?, ?)", (user_id, phone_number, username, "",
                                                                     self.get_chat_list_length()))

        self.increment_chat_list_length()

    def create_temp_new_user(self, phone_number, verification_code, username):
        self.DB.execute("INSERT INTO new_users VALUES (?, ?, ?, ?)", (phone_number, verification_code, time()+60,
                                                                      username))

        self.DB.commit()

    def create_chat(self, chat_id, chat_name, user_id):
        self.DB.execute("INSERT INTO chats VALUES (?, ?, ?, ?, ?, ?, ?)", (chat_id, "", chat_name, 0, 0,
                                                                           self.get_user_list_length(), user_id))
        self.increment_user_list_length()

    def delete_chat(self, chat_id):
        # delete user list
        user_list_id = self.get_chat_user_list_id(chat_id)
        self.DB.execute("DELETE FROM user_list WHERE user_list_id=" + str(user_list_id))

        # remove chat from user chat lists
        self.DB.execute("DELETE FROM chat_list WHERE chat_id=" + chat_id)

        # remove chat from table
        self.DB.execute("DELETE FROM chats WHERE chat_id=" + chat_id)

        self.DB.commit()

    """ CHAT LIST """

    def get_chat_list_length(self):
        return self.DB.execute("SELECT chat_list_length FROM counters").fetchone()[0]

    def increment_chat_list_length(self):
        chat_list_length = self.get_chat_list_length() + 1
        self.DB.execute("UPDATE counters SET chat_list_length=" + str(chat_list_length))

        self.DB.commit()

    def decrement_chat_list_length(self):
        chat_list_length = self.get_chat_list_length() - 1
        self.DB.execute("UPDATE counters SET chat_list_length=" + str(chat_list_length))

        self.DB.commit()

    """ USER LIST """

    def get_user_list_length(self):
        return self.DB.execute("SELECT user_list_length FROM counters").fetchone()[0]

    def increment_user_list_length(self):
        user_list_length = self.get_user_list_length() + 1
        self.DB.execute("UPDATE counters SET user_list_length=" + str(user_list_length))

        self.DB.commit()

    def decrement_user_list_length(self):
        user_list_length = self.get_user_list_length() - 1
        self.DB.execute("UPDATE counters SET user_list_length=" + str(user_list_length))

        self.DB.commit()

    """ USER """

    def get_user_id(self, phone_number):
        return self.DB.execute("SELECT user_id From users WHERE phone_number=" + phone_number).fetchone()[0]

    def get_user_phone_number(self, user_id):
        return self.DB.execute("SELECT phone_number From users WHERE user_id=" + str(user_id)).fetchone()[0]

    def set_user_phone_number(self, user_id, phone_number):
        self.DB.execute("UPDATE users SET phone_number=" + phone_number + " WHERE user_id=" + user_id)

        self.DB.commit()

    def get_user_chat_list(self, user_id):
        chat_list_id = self.get_user_chat_list_id(user_id)
        chat_list = self.DB.execute("SELECT chat_id FROM chat_list WHERE chat_list_id=" + str(chat_list_id)).fetchall()

        return_list = []
        for chat in chat_list:
            return_list.append(chat[0])

        return return_list

    def get_user_chat_list_id(self, user_id):
        return self.DB.execute("SELECT chat_list_id FROM users WHERE user_id=" + user_id).fetchone()[0]

    def add_to_user_chat_list(self, user_id, chat_id):
        chat_list_id = self.get_user_chat_list_id(user_id)

        self.DB.execute("INSERT INTO chat_list VALUES (?, ?)", (chat_list_id, chat_id))
        self.DB.commit()

    def remove_from_user_chat_list(self, user_id, chat_id):
        chat_list_id = self.get_user_chat_list_id(user_id)

        self.DB.execute("DELETE FROM chat_list WHERE chat_list_id=" + str(chat_list_id) + " AND chat_id=" + chat_id)
        self.DB.commit()

    def get_chat_user_count(self, chat_id):
        return self.DB.execute("SELECT user_count FROM chats WHERE chat_id=" + str(chat_id)).fetchone()[0]

    def set_chat_user_count(self, chat_id, user_count):
        self.DB.execute("UPDATE chats SET user_count=" + str(user_count) + " WHERE chat_id=" + chat_id)

        self.DB.commit()

    def add_user_to_chat(self, user_id, chat_id):
        """
        Adds user from a chat's user list
            - get id for chat's user list
            - get id for user's chat list
            - add user to chat list 
            - add chat to user list
            - increment user count
        :param user_id: user to add
        :param chat_id: chat to add to
        """

        self.add_to_user_chat_list(user_id, chat_id)
        self.add_to_chat_user_list(user_id, chat_id)
        user_count = self.get_chat_user_count(chat_id) + 1

        self.set_chat_user_count(chat_id, user_count)

    def check_if_phone_number_exists(self, phone_number):
        if self.DB.execute("SELECT user_id From users WHERE phone_number=" + phone_number).fetchone():
            return True

        return False

    def check_if_username_exists(self, username):
        if self.DB.execute("SELECT user_id From users WHERE username='" + username + "'").fetchone():
            return True

        return False

    def check_if_user_id_exists(self, user_id):
        if self.DB.execute("SELECT user_id From users WHERE user_id=" + user_id).fetchone():
            return True

        return False

    def remove_user_from_chat(self, user_id, chat_id):
        """
        Removes user from a chat's user list
            - get id for chat's user list
            - get id for user's chat list
            - remove user to chat list 
            - remove chat to user list
            - decrement user count
        :param user_id: user to remove
        :param chat_id: chat to remove from
        """

        self.remove_from_user_chat_list(user_id, chat_id)
        self.remove_from_chat_user_list(user_id, chat_id)
        user_count = self.get_chat_user_count(chat_id) - 1

        self.set_chat_user_count(chat_id, user_count)

    def check_if_user_is_in_chat(self, user_id, chat_id):
        chat_user_list = self.get_chat_user_list(chat_id)

        if user_id in chat_user_list:
            return True

        return False

    def get_user_username(self, user_id):
        return self.DB.execute("SELECT username FROM users WHERE user_id=" + user_id).fetchone()[0]

    """ CHAT """

    def get_chat_user_list(self, chat_id):
        user_list_id = self.get_chat_user_list_id(chat_id)
        user_list = self.DB.execute("SELECT user_id FROM user_list WHERE user_list_id=" + str(user_list_id)).fetchall()

        return_list = []
        for user in user_list:
            return_list.append(user[0])

        return return_list

    def get_chat_user_list_id(self, chat_id):
        return self.DB.execute("SELECT user_list_id FROM chats WHERE chat_id=" + str(chat_id)).fetchone()[0]

    def add_to_chat_user_list(self, user_id, chat_id):
        user_list_id = self.get_chat_user_list_id(chat_id)

        self.DB.execute("INSERT INTO user_list VALUES (?, ?, ?, ?)", (user_list_id, user_id, -1, -1))
        self.DB.commit()

    def remove_from_chat_user_list(self, user_id, chat_id):
        user_list_id = self.get_chat_user_list_id(chat_id)

        self.DB.execute("DELETE FROM user_list WHERE user_list_id=" + str(user_list_id) + " AND user_id=" + user_id)
        self.DB.commit()

    def set_chat_name(self, chat_id, new_chat_name):
        self.DB.execute("UPDATE chats SET chat_name='" + new_chat_name + "' WHERE chat_id=" + chat_id)

        self.DB.commit()

    def get_message_count(self, chat_id):
        return self.DB.execute("SELECT message_count FROM chats WHERE chat_id=" + chat_id).fetchone()[0]

    def increment_chat_message_count(self, chat_id, message_count):
        self.DB.execute("UPDATE chats SET message_count=" + str(message_count + 1) + " WHERE chat_id=" + chat_id)

    def check_if_chat_id_exists(self, chat_id):
        if self.DB.execute("SELECT chat_id From chats WHERE chat_id=" + chat_id).fetchone():
            return True

        return False

    def get_chat_admin_user_id(self, chat_id):
        return self.DB.execute("SELECT admin_user_id FROM chats WHERE chat_id=" + chat_id).fetchone()[0]

    def get_new_chat_names(self, user_id):
        chat_ids = []

        user_list_ids = self.DB.execute("SELECT user_list_id FROM user_list WHERE user_id=" + user_id + " AND last_sent_message_id=-1").fetchall()
        for user_list_id in user_list_ids:
            chat_ids += self.DB.execute("SELECT chat_name FROM chats WHERE user_list_id=" + str(user_list_id[0])).fetchall()
        return chat_ids

    def get_chat_name(self, chat_id):
        return self.DB.execute("SELECT chat_name FROM chats WHERE chat_id=" + chat_id).fetchone()[0]

    """ MESSAGES """

    def send_message(self, src_id, chat_id, lat, lng, message_data):
        """
        Add message to message database
        :param src_id: sender id
        :param chat_id: chat to send message to
        :param lat: latitude of message
        :param lng: longitude of message
        :param message_data: message_data
        :return: message_id
        """
        message_id = self.get_message_count(chat_id)

        self.increment_chat_message_count(chat_id, message_id)

        self.DB.execute("INSERT INTO messages VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", (chat_id, str(message_id), src_id,
                                                                                    "", lat, lng, message_data, 0, 0))
        self.DB.commit()

        return message_id

    def get_all_unread_messages(self, user_id):
        """
        Get all unread messages for users
            - get chat list
            - add messages to list
            - increment sent counter
            - return list
        :param user_id: 
        :return: 
        """

        chat_list = self.get_user_chat_list(user_id)
        unread_messages = []

        for chat_id in chat_list:
            chat_id = str(chat_id)
            user_list_id = self.get_chat_user_list_id(chat_id)
            last_message_sent = self.get_last_message_sent_id(user_list_id, user_id)
            # Get Messages
            messages = self.DB.execute("SELECT * FROM messages WHERE chat_id=" + chat_id + " AND message_id>" +
                                       str(last_message_sent)).fetchall()
            if messages:
                last_message_sent_id = messages[-1][1]
                self.set_last_message_sent_id(user_list_id, user_id, last_message_sent_id)

                # get the amount of users that message was sent to
                sent_counter = len(self.DB.execute("SELECT user_list_id FROM user_list WHERE user_list_id=" +
                                                   str(user_list_id) + " AND last_sent_message_id=" +
                                                   str(last_message_sent_id)).fetchall())

                # get user count for chat
                user_count = self.get_chat_user_count(chat_id)

                # if everyone go message then delete message
                if sent_counter == user_count:
                    self.DB.execute("DELETE FROM messages WHERE chat_id=" + chat_id)

            self.DB.commit()

            unread_messages.extend(messages)

        return unread_messages

    def mark_read_messages(self, user_id, chat_id, last_message_read):
        users_list_id = self.get_chat_user_list_id(chat_id)
        self.set_last_message_read_id(users_list_id, user_id, last_message_read)

    def update_message_sent_counter(self, chat_id, message_id, value):
        self.DB.execute("UPDATE messages SET sent=" + str(value) + " WHERE chat_id=" + str(chat_id) + " AND message_id="
                        + str(message_id))

        self.DB.commit()

    """ LISTS """

    def get_last_message_sent_id(self, user_list_id, user_id):
        return self.DB.execute("SELECT last_sent_message_id FROM user_list WHERE user_list_id=" + str(user_list_id) +
                               " AND user_id=" + user_id).fetchone()[0]

    def set_last_message_sent_id(self, user_list_id, user_id, message_id):
        self.DB.execute("UPDATE user_list SET last_sent_message_id=" + str(message_id) + " WHERE user_list_id=" +
                        str(user_list_id) + " AND user_id=" + user_id)

        self.DB.commit()

    def set_last_message_read_id(self, user_list_id, user_id, message_id):
        self.DB.execute("UPDATE user_list SET last_read_message_id=" + message_id + " WHERE user_list_id=" +
                        str(user_list_id) + " AND user_id=" + user_id)

        self.DB.commit()

    """ NEW USERS """

    def get_new_user(self, phone_number):
        return self.DB.execute("SELECT * FROM new_users WHERE phone_number=" + phone_number).fetchall()

    def remove_new_user(self, phone_number):
        self.DB.execute("DELETE FROM new_users WHERE phone_number=" + phone_number)

        self.DB.commit()

    def update_verification_code(self, phone_number, verification_code):
        self.DB.execute("UPDATE new_users SET verification_code= " + verification_code + ", time_to_destroy="
                        + str(time()+60) + " WHERE phone_number=" + phone_number)

        self.DB.commit()
