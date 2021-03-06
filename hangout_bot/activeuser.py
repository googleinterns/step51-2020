class ActiveUser:
    def __init__(self, user_id):
        self.user_id = user_id
        self.phase_num = -1
        self.campaign_name = ''
        self.keyword_campaign_id = ''
        self.accepting_text = False
        self.editing = False
    
    def set_keyword_campaign_id(self, keyword_campaign_id):
        self.keyword_campaign_id = keyword_campaign_id

    def set_accepting_text(self, accepting):
        self.accepting_text = accepting
    
    def set_campaign_name(self, campaign_name):
        self.campaign_name = campaign_name

    def set_phase_num(self, phase_num):
        self.phase_num = phase_num  
    
    def set_editing(self, editing):
        self.editing = editing

    def increment_phase_num(self):
        self.phase_num = self.phase_num + 1 
  
