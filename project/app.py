import os
from datetime import datetime, date
from flask import Flask, render_template, request, redirect, url_for, flash, session, g, make_response
from flask_sqlalchemy import SQLAlchemy

# Initialize Flask App
app = Flask(__name__)

# Configure Secret Key for Sessions
app.secret_key = os.environ.get('FLASK_SECRET_KEY', 'blood_donor_secret_key_123_abc')

# Configure database directory
db_dir = os.path.join(app.root_path, 'database')
if not os.path.exists(db_dir):
    os.makedirs(db_dir)

# Configure SQLite Database - stored permanently in database/donors.db
db_path = os.path.join(db_dir, 'donors.db')
app.config['SQLALCHEMY_DATABASE_URI'] = f'sqlite:///{db_path}'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# Initialize SQLAlchemy
db = SQLAlchemy(app)

# --- DATABASE MODELS ---

class Donor(db.Model):
    __tablename__ = 'donors'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), nullable=False)
    district = db.Column(db.String(50), nullable=False)
    city = db.Column(db.String(50), nullable=False)
    blood_group = db.Column(db.String(5), nullable=False)
    gender = db.Column(db.String(10), nullable=False)
    dob = db.Column(db.String(10), nullable=False) # Store as YYYY-MM-DD
    age = db.Column(db.Integer, nullable=False)
    phone = db.Column(db.String(15), unique=True, nullable=False)
    email = db.Column(db.String(100), nullable=False)
    availability = db.Column(db.String(15), default='Available') # 'Available' or 'Unavailable'
    last_donation_date = db.Column(db.String(10), nullable=True) # Store as YYYY-MM-DD or empty
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    def __init__(self, name, district, city, blood_group, gender, dob, age, phone, email, availability, last_donation_date=None):
        self.name = name
        self.district = district
        self.city = city
        self.blood_group = blood_group
        self.gender = gender
        self.dob = dob
        self.age = age
        self.phone = phone
        self.email = email
        self.availability = availability
        self.last_donation_date = last_donation_date

class EmergencyRequest(db.Model):
    __tablename__ = 'emergency_requests'
    
    id = db.Column(db.Integer, primary_key=True)
    patient_name = db.Column(db.String(100), nullable=False)
    blood_group = db.Column(db.String(5), nullable=False)
    hospital_name = db.Column(db.String(150), nullable=False)
    location = db.Column(db.String(100), nullable=False) # City/District
    urgency_level = db.Column(db.String(15), nullable=False) # 'Critical', 'Urgent', 'Normal'
    contact_phone = db.Column(db.String(15), nullable=False)
    message = db.Column(db.Text, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

# --- UTILITIES & MIDDLEWARE ---

# Karnataka Districts Comprehensive List
KARNATAKA_DISTRICTS = [
    "Bengaluru Urban", "Bengaluru Rural", "Mysuru", "Mandya", "Hassan",
    "Shivamogga", "Dakshina Kannada", "Udupi", "Tumakuru", "Ballari",
    "Belagavi", "Dharwad", "Kolar", "Chikkamagaluru", "Raichur",
    "Bidar", "Kalaburagi", "Kodagu", "Bagalkote", "Chamarajanagar",
    "Chikkaballapur", "Chitradurga", "Davanagere", "Gadag", "Haveri",
    "Koppal", "Ramanagara", "Uttara Kannada", "Vijayapura", "Yadgir",
    "Vijayanagara"
]

# Blood groups configuration
BLOOD_GROUPS = ["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"]

# Helper to load current user (admin details)
@app.before_request
def load_logged_in_user():
    g.admin = session.get('admin_user')

# Login required decorator
def login_required(view):
    from functools import wraps
    @wraps(view)
    def wrapped_view(**kwargs):
        if not g.admin:
            flash("Unauthorized Access! Admin login required.", "danger")
            return redirect(url_for('login'))
        return view(**kwargs)
    return wrapped_view

# Helper to calculate age from DOB string
def calculate_age(dob_str):
    try:
        born = datetime.strptime(dob_str, '%Y-%m-%d').date()
        today = date.today()
        # Correctly account for leap years and birthdays later in the year
        return today.year - born.year - ((today.month, today.day) < (born.month, born.day))
    except Exception:
        return 0

# --- ROUTES ---

# 1. Public - Search Donors screen (Home)
@app.route('/')
def index():
    # Fetch parameters
    query_bg = request.args.get('blood_group', '')
    query_dist = request.args.get('district', '')
    query_city = request.args.get('city', '')
    query_avail = request.args.get('availability', '')

    donor_query = Donor.query

    # Apply Filters
    if query_bg:
        donor_query = donor_query.filter(Donor.blood_group == query_bg)
    if query_dist:
        donor_query = donor_query.filter(Donor.district == query_dist)
    if query_city:
        donor_query = donor_query.filter(Donor.city.like(f"%{query_city}%"))
    if query_avail:
        donor_query = donor_query.filter(Donor.availability == query_avail)

    donors = donor_query.order_by(Donor.name.asc()).all()
    count = len(donors)

    # Calculate overall sidebar stats
    total_donors = Donor.query.count()
    available_donors = Donor.query.filter_by(availability='Available').count()
    recent_requests = EmergencyRequest.query.order_by(EmergencyRequest.created_at.desc()).limit(3).all()

    return render_template('index.html', 
                           donors=donors, 
                           count=count, 
                           districts=KARNATAKA_DISTRICTS, 
                           blood_groups=BLOOD_GROUPS,
                           selected_bg=query_bg,
                           selected_dist=query_dist,
                           selected_city=query_city,
                           selected_avail=query_avail,
                           total_donors=total_donors,
                           available_donors=available_donors,
                           recent_requests=recent_requests)

# 2. Admin Authentication - Login
@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        admin_id = request.form.get('admin_id')
        password = request.form.get('password')

        # Hardcoded credentials from guideline requirements:
        # Admin ID: Blooddonorssystem
        # Password: blooddonor@123
        if admin_id == "Blooddonorssystem" and password == "blooddonor@123":
            session.clear()
            session['admin_user'] = "Admin"
            flash("Successfully logged in as Admin!", "success")
            return redirect(url_for('dashboard'))
        else:
            flash("Invalid Admin ID or Password. Please try again.", "danger")
            
    return render_template('login.html')

# 3. Admin Authentication - Logout
@app.route('/logout')
def logout():
    session.clear()
    flash("Successfully logged out.", "info")
    return redirect(url_for('index'))

# 4. Admin - Dashboard
@app.route('/dashboard')
@login_required
def dashboard():
    donors = Donor.query.order_by(Donor.created_at.desc()).all()
    total_donors = len(donors)
    available = Donor.query.filter_by(availability='Available').count()
    
    # Blood Group Statistics
    bg_stats = {}
    for bg in BLOOD_GROUPS:
        bg_stats[bg] = Donor.query.filter_by(blood_group=bg).count()
        
    # District Statistics
    dist_stats = {}
    for dist in KARNATAKA_DISTRICTS:
        cnt = Donor.query.filter_by(district=dist).count()
        if cnt > 0:
            dist_stats[dist] = cnt

    # System Statuses
    recent_donors = Donor.query.order_by(Donor.created_at.desc()).limit(5).all()
    emergency_reqs = EmergencyRequest.query.order_by(EmergencyRequest.created_at.desc()).all()
    total_emergencies = len(emergency_reqs)

    return render_template('dashboard.html',
                           donors=donors,
                           total_donors=total_donors,
                           available_donors=available,
                           bg_stats=bg_stats,
                           dist_stats=dist_stats,
                           recent_donors=recent_donors,
                           emergency_reqs=emergency_reqs,
                           total_emergencies=total_emergencies)

# 5. Admin - Donor Registration (Create API)
@app.route('/register', methods=['GET', 'POST'])
@login_required
def register():
    if request.method == 'POST':
        name = request.form.get('name', '').strip()
        district = request.form.get('district', '').strip()
        city = request.form.get('city', '').strip()
        blood_group = request.form.get('blood_group', '').strip()
        gender = request.form.get('gender', '').strip()
        dob = request.form.get('dob', '').strip()
        phone = request.form.get('phone', '').strip()
        email = request.form.get('email', '').strip()
        availability = request.form.get('availability', 'Available')
        last_donation_date = request.form.get('last_donation_date', '').strip() or None

        # --- Form Validations ---
        if not name or not district or not city or not blood_group or not dob or not phone or not email:
            flash("All fields marked with an asterisk (*) are strictly required.", "danger")
            return redirect(url_for('register'))

        # Phone validation (10 digits)
        clean_phone = ''.join(filter(str.isdigit, phone))
        if len(clean_phone) != 10:
            flash("Invalid Phone number. Must contain exactly 10 digits.", "danger")
            return redirect(url_for('register'))

        # Prevent duplicates
        existing_donor = Donor.query.filter_by(phone=phone).first()
        if existing_donor:
            flash(f"Error: A donor with phone number {phone} is already registered.", "danger")
            return redirect(url_for('register'))

        # Auto-calculate age
        age = calculate_age(dob)
        if age < 18 or age > 65:
            flash("Donor age must be between 18 and 65 years old to donate blood.", "danger")
            return redirect(url_for('register'))

        try:
            # Create object & store in Database
            new_donor = Donor(
                name=name, district=district, city=city, blood_group=blood_group,
                gender=gender, dob=dob, age=age, phone=phone, email=email,
                availability=availability, last_donation_date=last_donation_date
            )
            db.session.add(new_donor)
            db.session.commit()
            
            flash(f"Successfully registered blood donor: {name}!", "success")
            return redirect(url_for('dashboard'))
        except Exception as e:
            db.session.rollback()
            flash(f"Database insertion failed: {str(e)}", "danger")

    return render_template('register.html', districts=KARNATAKA_DISTRICTS, blood_groups=BLOOD_GROUPS)

# 6. Admin - Edit Donor (Update API)
@app.route('/edit/<int:id>', methods=['GET', 'POST'])
@login_required
def edit(id):
    donor = Donor.query.get_or_404(id)
    
    if request.method == 'POST':
        name = request.form.get('name', '').strip()
        district = request.form.get('district', '').strip()
        city = request.form.get('city', '').strip()
        blood_group = request.form.get('blood_group', '').strip()
        gender = request.form.get('gender', '').strip()
        dob = request.form.get('dob', '').strip()
        phone = request.form.get('phone', '').strip()
        email = request.form.get('email', '').strip()
        availability = request.form.get('availability', 'Available')
        last_donation_date = request.form.get('last_donation_date', '').strip() or None

        if not name or not district or not city or not blood_group or not dob or not phone or not email:
            flash("Required fields must not be empty.", "danger")
            return redirect(url_for('edit', id=id))

        # Validate unique phone (except own entry)
        duplicate_phone_and_not_self = Donor.query.filter(Donor.phone == phone, Donor.id != id).first()
        if duplicate_phone_and_not_self:
            flash(f"Error: Phone number {phone} is already linked to another registered donor.", "danger")
            return redirect(url_for('edit', id=id))

        # Calculate Age
        age = calculate_age(dob)

        try:
            donor.name = name
            donor.district = district
            donor.city = city
            donor.blood_group = blood_group
            donor.gender = gender
            donor.dob = dob
            donor.age = age
            donor.phone = phone
            donor.email = email
            donor.availability = availability
            donor.last_donation_date = last_donation_date
            
            db.session.commit()
            flash(f"Successfully updated donor registration record for {name}.", "success")
            return redirect(url_for('dashboard'))
        except Exception as e:
            db.session.rollback()
            flash(f"Database update failed: {str(e)}", "danger")

    return render_template('edit.html', donor=donor, districts=KARNATAKA_DISTRICTS, blood_groups=BLOOD_GROUPS)

# 7. Admin - Delete Donor (Delete API)
@app.route('/delete/<int:id>', methods=['POST'])
@login_required
def delete(id):
    donor = Donor.query.get_or_404(id)
    try:
        db.session.delete(donor)
        db.session.commit()
        flash(f"Successfully deleted donor record for {donor.name}.", "success")
    except Exception as e:
        db.session.rollback()
        flash(f"Failed to delete record: {str(e)}", "danger")
        
    return redirect(url_for('dashboard'))

# 8. Emergency Blood Requests Screen (View & Create API)
@app.route('/emergency', methods=['GET', 'POST'])
def emergency():
    if request.method == 'POST':
        patient_name = request.form.get('patient_name', '').strip()
        blood_group = request.form.get('blood_group', '').strip()
        hospital_name = request.form.get('hospital_name', '').strip()
        location = request.form.get('location', '').strip()
        urgency_level = request.form.get('urgency_level', 'Normal').strip()
        contact_phone = request.form.get('contact_phone', '').strip()
        message = request.form.get('message', '').strip()

        if not patient_name or not blood_group or not hospital_name or not location or not contact_phone:
            flash("Please fill in all mandatory fields for the emergency blood request.", "danger")
            return redirect(url_for('emergency'))

        try:
            req = EmergencyRequest(
                patient_name=patient_name, blood_group=blood_group,
                hospital_name=hospital_name, location=location,
                urgency_level=urgency_level, contact_phone=contact_phone,
                message=message
            )
            db.session.add(req)
            db.session.commit()
            flash("EMERGENCY BLOOD REQUEST REGISTERED SUCCESSFULLY! Nearby donors will be notified.", "success")
            return redirect(url_for('emergency'))
        except Exception as e:
            db.session.rollback()
            flash(f"Request could not be published: {str(e)}", "danger")

    requests = EmergencyRequest.query.order_by(EmergencyRequest.created_at.desc()).all()
    return render_template('emergency.html', requests=requests, blood_groups=BLOOD_GROUPS)

# 9. Admin - Delete Emergency Request
@app.route('/delete_emergency/<int:id>', methods=['POST'])
@login_required
def delete_emergency(id):
    req = EmergencyRequest.query.get_or_404(id)
    try:
        db.session.delete(req)
        db.session.commit()
        flash("Emergency request successfully completed and resolved.", "success")
    except Exception as e:
        db.session.rollback()
        flash(f"Could not delete emergency request: {str(e)}", "danger")
    return redirect(url_for('dashboard'))

# 10. Admin - Export Donor Data as CSV
@app.route('/export-csv')
@login_required
def export_csv():
    import csv
    from io import StringIO

    donors = Donor.query.order_by(Donor.name.asc()).all()
    
    output = StringIO()
    writer = csv.writer(output)
    
    # Write Headers
    writer.writerow([
        'ID', 'FullName', 'District', 'City', 'BloodGroup', 
        'Gender', 'DOB', 'Age', 'Phone', 'Email', 
        'AvailabilityStatus', 'LastDonationDate', 'RegistrationDate'
    ])
    
    for d in donors:
        writer.writerow([
            d.id, d.name, d.district, d.city, d.blood_group,
            d.gender, d.dob, d.age, d.phone, d.email,
            d.availability, d.last_donation_date or 'N/A', d.created_at.strftime('%Y-%m-%d %H:%M:%S')
        ])

    response = make_response(output.getvalue())
    response.headers["Content-Disposition"] = f"attachment; filename=Karnataka_Blood_Donors_{datetime.now().strftime('%Y%m%d')}.csv"
    response.headers["Content-type"] = "text/csv"
    return response

# --- CREATE DATABASE & INSERT SEED DATA ---

def insert_seed_data():
    # Only populate if database is entirely empty
    if Donor.query.count() == 0:
        sample_donors = [
            Donor("Anish Kumar", "Bengaluru Urban", "Koramanagala", "O+", "Male", "1994-04-12", 32, "9876543210", "anish.k@gmail.com", "Available", "2026-01-10"),
            Donor("Kavitha Rao", "Mysuru", "Gokulam", "B+", "Female", "1997-08-25", 28, "8765432109", "kavitha.mysore@yahoo.com", "Available", "2025-11-15"),
            Donor("Mohammed Tariq", "Dakshina Kannada", "Mangaluru Port", "A-", "Male", "1991-12-05", 34, "7654321098", "tariq.mang@gmail.com", "Available", None),
            Donor("Priya Deshpande", "Dharwad", "Hubballi West", "AB+", "Female", "1999-03-30", 27, "6543210987", "priya_desh@hotmail.com", "Unavailable", "2026-05-01"),
            Donor("Raghavendra Bhat", "Udupi", "Manipal Medical", "O-", "Male", "1988-10-15", 37, "9900112233", "raghu.udupi@gmail.com", "Available", "2025-09-05"),
            Donor("Suma Gowda", "Mandya", "Sanjay Circle", "A+", "Female", "1996-05-18", 30, "7766554433", "suma.mandya@gmail.com", "Available", "2026-02-14"),
        ]
        for donor in sample_donors:
            db.session.add(donor)
            
        sample_requests = [
            EmergencyRequest(
                patient_name="Shankarappa Hegde", blood_group="O+", 
                hospital_name="Sanjay Gandhi Institute of Trauma", location="Bengaluru Urban",
                urgency_level="Critical", contact_phone="9844001122", 
                message="Patient undergoing cardiac bypass surgery tomorrow morning. Urgently requires 3 units of O+ blood."
            ),
            EmergencyRequest(
                patient_name="Siddaramaiah KM", blood_group="O-", 
                hospital_name="K.R. Hospital", location="Mysuru",
                urgency_level="Urgent", contact_phone="8971002233", 
                message="Sepsis patient under vital care, O- blood highly preferred immediately."
            ),
        ]
        for req in sample_requests:
            db.session.add(req)

        db.session.commit()
        print("Successfully seeded data for Karnataka Blood Donor Management System!")

with app.app_context():
    db.create_all()
    insert_seed_data()

# Launching main server block (standard configuration)
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
