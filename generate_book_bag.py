import json
import uuid
from datetime import datetime, timedelta

def get_wednesdays(start_date, end_date):
    d = start_date
    while d.weekday() != 2:  # Wednesday is 2
        d += timedelta(days=1)
    
    wednesdays = []
    while d <= end_date:
        wednesdays.append(d)
        d += timedelta(days=7)
    return wednesdays

# Half terms
half_terms = [
    (datetime(2026, 2, 16), datetime(2026, 2, 20)),
    (datetime(2026, 5, 25), datetime(2026, 5, 29))
]

def is_half_term(date):
    for start, end in half_terms:
        if start <= date <= end:
            return True
    return False

start = datetime(2026, 1, 29) # Today
end = datetime(2026, 7, 17)

wednesdays = get_wednesdays(start, end)
valid_weds = [w for w in wednesdays if not is_half_term(w)]

new_events = []
for w in valid_weds:
    new_events.append({
        "id": str(uuid.uuid4()),
        "title": "Book bag (return)",
        "startDate": f"{w.strftime('%Y-%m-%d')}T09:00:00",
        "endDate": f"{w.strftime('%Y-%m-%d')}T09:00:00",
        "allDay": False,
        "notes": "Return book bag to school.",
        "confidence": 1.0,
        "status": "ACTIVE",
        "isRecurring": True,
        "sourceEmailId": "manual-entry",
        "sourceEmailSubject": "Manual Entry",
        "sourceEmailReceivedAt": datetime.now().strftime('%Y-%m-%dT%H:%M:%S')
    })

# Load existing
with open('output/events.json', 'r') as f:
    events = json.load(f)

# Append
events.extend(new_events)

# Save
with open('output/events.json', 'w') as f:
    json.dump(events, f, indent=2)

print(f"Added {len(new_events)} events.")
