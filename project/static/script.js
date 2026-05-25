// Static JavaScript functionality for Blood Donor Management System

document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    initAgeCalculator();
    initAlertAutoDismiss();
    initSearchHistory();
});

// 1. Dark Mode System
function initTheme() {
    const toggleSwitch = document.querySelector('#theme-checkbox');
    const currentTheme = localStorage.getItem('theme');

    if (currentTheme) {
        document.documentElement.setAttribute('data-theme', currentTheme);
        if (currentTheme === 'dark' && toggleSwitch) {
            toggleSwitch.checked = true;
        }
    }

    if (toggleSwitch) {
        toggleSwitch.addEventListener('change', (e) => {
            if (e.target.checked) {
                document.documentElement.setAttribute('data-theme', 'dark');
                localStorage.setItem('theme', 'dark');
            } else {
                document.documentElement.setAttribute('data-theme', 'light');
                localStorage.setItem('theme', 'light');
            }
        });
    }
}

// 2. Real-time Age Calculation
function initAgeCalculator() {
    const dobInput = document.getElementById('dob');
    const ageInput = document.getElementById('calculated_age');

    if (dobInput && ageInput) {
        dobInput.addEventListener('change', () => {
            const dobValue = dobInput.value;
            if (dobValue) {
                const age = calculateAge(dobValue);
                ageInput.value = age + " Years";
                
                // Show a helpful badge depending on suitability
                const feedbackText = document.getElementById('age_feedback');
                if (feedbackText) {
                    if (age >= 18 && age <= 65) {
                        feedbackText.innerHTML = `<span class="text-success"><i class="bi bi-patch-check-fill"></i> Eligible to donate blood (Age ${age})</span>`;
                    } else {
                        feedbackText.innerHTML = `<span class="text-danger"><i class="bi bi-exclamation-triangle-fill"></i> Ineligible: Age must be 18 to 65 years (Age ${age})</span>`;
                    }
                }
            }
        });
    }
}

function calculateAge(dobStr) {
    const birthday = new Date(dobStr);
    const today = new Date();
    let age = today.getFullYear() - birthday.getFullYear();
    const monthDiff = today.getMonth() - birthday.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthday.getDate())) {
        age--;
    }
    return age;
}

// 3. Auto Dismissing Flash Toast Notifications
function initAlertAutoDismiss() {
    setTimeout(() => {
        const alerts = document.querySelectorAll('.alert-dismissible');
        alerts.forEach(alert => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 4500);
}

// 4. Client Side PDF Generation via jsPDF
function generateDonorPDF(title = "Karnataka Blood Donors List") {
    // Read the jsPDF from window loading
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF('p', 'mm', 'a4');
    
    // Theme colors for document styling
    const headerColor = [211, 47, 47]; // RGB primary red #D32F2F
    const textColor = [33, 37, 41];
    
    // Main Document Header Decoration
    doc.setFillColor(...headerColor);
    doc.rect(0, 0, 210, 35, 'F');
    
    doc.setFont("helvetica", "bold");
    doc.setFontSize(22);
    doc.setTextColor(255, 255, 255);
    doc.text("BLOOD DONOR MANAGEMENT SYSTEM", 15, 18);
    
    doc.setFont("helvetica", "normal");
    doc.setFontSize(11);
    doc.text("State: Karnataka, India  |  Academic Student Major Project Report", 15, 26);
    
    // Timestamp line
    const now = new Date();
    const generatedOn = now.toLocaleDateString() + ' ' + now.toLocaleTimeString();
    
    doc.setFont("helvetica", "normal");
    doc.setFontSize(10);
    doc.setTextColor(100, 100, 100);
    doc.text(`Report Title: ${title}`, 15, 45);
    doc.text(`Generated On: ${generatedOn}`, 15, 50);
    doc.text("----------------------------------------------------------------------------------------------------------------", 15, 54);
    
    // Draw columns helper heading
    doc.setFont("helvetica", "bold");
    doc.setFontSize(10);
    doc.setTextColor(...textColor);
    
    // Headers
    doc.text("No.", 12, 60);
    doc.text("Donor Name", 25, 60);
    doc.text("Blood GP", 65, 60);
    doc.text("District & City", 90, 60);
    doc.text("Contact No.", 145, 60);
    doc.text("Status", 180, 60);
    
    doc.text("----------------------------------------------------------------------------------------------------------------", 15, 63);
    
    // Read elements from the current UI Table
    const tableRows = document.querySelectorAll('.pdf-target-row');
    let yOffset = 70;
    
    if (tableRows.length === 0) {
        doc.setFont("helvetica", "italic");
        doc.setTextColor(150, 150, 150);
        doc.text("No donors listed in the current filtered query.", 20, 75);
    } else {
        doc.setFont("helvetica", "normal");
        doc.setFontSize(9);
        doc.setTextColor(...textColor);
        
        tableRows.forEach((row, index) => {
            // Avoid overflow across current page
            if (yOffset > 275) {
                doc.addPage();
                yOffset = 25;
                doc.setFont("helvetica", "bold");
                doc.setFontSize(10);
                doc.text("No.", 12, yOffset);
                doc.text("Donor Name", 25, yOffset);
                doc.text("Blood GP", 65, yOffset);
                doc.text("District & City", 90, yOffset);
                doc.text("Contact No.", 145, yOffset);
                doc.text("Status", 180, yOffset);
                doc.text("----------------------------------------------------------------------------------------------------------------", 15, yOffset + 3);
                yOffset += 10;
                doc.setFont("helvetica", "normal");
                doc.setFontSize(9);
            }
            
            const name = row.getAttribute('data-name') || "N/A";
            const bg = row.getAttribute('data-bg') || "N/A";
            const district = row.getAttribute('data-district') || "N/A";
            const city = row.getAttribute('data-city') || "N/A";
            const phone = row.getAttribute('data-phone') || "N/A";
            const availability = row.getAttribute('data-avail') || "Available";
            
            // Clean strings
            const loc = `${city}, ${district}`.substring(0, 28);
            const donorName = name.substring(0, 22);
            
            doc.text((index + 1).toString(), 12, yOffset);
            doc.text(donorName, 25, yOffset);
            
            // Highlight blood group with bold font
            doc.setFont("helvetica", "bold");
            doc.text(bg, 68, yOffset);
            doc.setFont("helvetica", "normal");
            
            doc.text(loc, 90, yOffset);
            doc.text(phone, 145, yOffset);
            doc.text(availability, 180, yOffset);
            
            yOffset += 8;
        });
    }
    
    // Add page footer with total pages
    const pageCount = doc.internal.getNumberOfPages();
    for (let i = 1; i <= pageCount; i++) {
        doc.setPage(i);
        doc.setFontSize(8);
        doc.setTextColor(150, 150, 150);
        doc.text(`Page ${i} of ${pageCount}  |  Blood Donor Management System - Karnataka`, 15, 288);
        doc.text("Project Authored by Engineering Student for Year-V Academic Review", 120, 288);
    }
    
    // Download the PDF
    doc.save(`Karnataka_Blood_Donors_Report_${now.getFullYear()}${String(now.getMonth()+1).padStart(2,'0')}${String(now.getDate()).padStart(2,'0')}.pdf`);
}

// 5. Search History tracker using Client storage
function initSearchHistory() {
    const searchHistoryList = document.getElementById('search_history_items');
    if (!searchHistoryList) return;

    // Load and render history
    renderSearchHistory();

    const searchForm = document.getElementById('searchForm');
    if (searchForm) {
        searchForm.addEventListener('submit', () => {
            const bg = document.getElementById('blood_group')?.value;
            const dist = document.getElementById('district')?.value;
            const city = document.getElementById('city')?.value;

            if (bg || dist || city) {
                const searchCriteria = {
                    bg: bg || "Any Group",
                    dist: dist || "Any District",
                    city: city || "",
                    timestamp: new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})
                };

                let history = [];
                try {
                    history = JSON.parse(localStorage.getItem('donorSearchHistory') || '[]');
                } catch(e) {
                    history = [];
                }

                // Append and slice
                history.unshift(searchCriteria);
                history = history.slice(0, 5); // Max 5 items
                localStorage.setItem('donorSearchHistory', JSON.stringify(history));
            }
        });
    }
}

function renderSearchHistory() {
    const container = document.getElementById('search_history_items');
    if (!container) return;

    let history = [];
    try {
        history = JSON.parse(localStorage.getItem('donorSearchHistory') || '[]');
    } catch(e) {
        history = [];
    }

    if (history.length === 0) {
        container.innerHTML = `<li class="list-group-item text-muted text-center py-2" style="font-size:0.85rem;">No recent searches</li>`;
        return;
    }

    let html = '';
    history.forEach(item => {
        let label = `<strong>${item.bg}</strong> in <strong>${item.dist}</strong>`;
        if (item.city) label += ` (${item.city})`;
        
        html += `
            <li class="list-group-item d-flex justify-content-between align-items-center py-2" style="font-size:0.85rem;">
                <span><i class="bi bi-clock-history me-1"></i> ${label}</span>
                <span class="badge bg-secondary rounded-pill text-white">${item.timestamp}</span>
            </li>
        `;
    });
    container.innerHTML = html;
}

function clearSearchHistory() {
    localStorage.removeItem('donorSearchHistory');
    renderSearchHistory();
}
