class ServerError(Exception):
    def __init__(self, message):
        self.error_message = message

    def get_message(self):
        return self.error_message


class ServerMessage(Exception):
    def __init__(self, message):
        self.message = message

    def get_message(self):
        return self.message
