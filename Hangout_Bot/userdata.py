class ActiveUser:
    def __init__(self, user_id):
        self.user_id = user_id
        self.phase_num = 0
        self.campaign_name = ''
        self.accepting_text = True
    
    def set_accepting_text(self, accepting):
        self.accepting_text = accepting
    
    def set_campaign_name(self, campaign_name):
        self.campaign_name = campaign_name

    def set_phase_num(self, phase_num):
        self.phase_num = phase_num  

    def append_phase_num(self):
        self.phase_num = self.phase_num + 1 
