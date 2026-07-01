package com.example.torque.garage

object MarcasModelosCoche {
    val TODAS = mapOf(
        "Alfa Romeo" to listOf("Giulia", "Stelvio", "Tonale", "Giulietta", "MiTo", "159", "Brera", "Spider", "4C", "33 Stradale"),
        "Audi" to listOf("A1", "A3", "A4", "A5", "A6", "A7", "A8", "Q2", "Q3", "Q5", "Q7", "Q8", "Q8 e-tron", "e-tron GT", "TT", "R8"),
        "BMW" to listOf("Serie 1", "Serie 2", "Serie 3", "Serie 4", "Serie 5", "Serie 6", "Serie 7", "X1", "X2", "X3", "X4", "X5", "X6", "X7", "i4", "iX", "iX3", "Z4", "M3", "M4", "M5"),
        "Citroën" to listOf("C1", "C2", "C3", "C3 Aircross", "C4", "C4 Cactus", "C4 SpaceTourer", "C5", "C5 Aircross", "C6", "Berlingo", "Jumpy", "Jumper", "DS3", "DS4", "DS5", "AMI", "ë-C4"),
        "Cupra" to listOf("Ibiza", "Leon", "Leon Sportstourer", "Ateca", "Formentor", "Born", "Tavascan"),
        "Dacia" to listOf("Sandero", "Sandero Stepway", "Logan", "Duster", "Jogger", "Spring (eléctrico)", "Bigster"),
        "Fiat" to listOf("500", "500L", "500X", "500e", "Panda", "Punto", "Tipo", "Tipo Cross", "Doblo", "Scudo", "Ducato", "Fullback", "Talento", "124 Spider", "Barchetta", "Coupé"),
        "Ford" to listOf("Fiesta", "Focus", "Focus ST", "Mondeo", "Kuga", "Puma", "Mustang", "Mustang Mach-E", "Ranger", "Transit", "Tourneo", "Galaxy", "S-MAX", "Edge", "Explorer", "EcoSport"),
        "Honda" to listOf("Civic", "Civic Type R", "HR-V", "CR-V", "Jazz", "e (eléctrico)", "Accord", "NSX", "CR-Z", "Legend", "FR-V", "Stream"),
        "Hyundai" to listOf("i10", "i20", "i30", "i30 N", "i40", "Bayon", "Kona", "Kona Electric", "Tucson", "Santa Fe", "IONIQ 5", "IONIQ 6", "NEXO", "Staria", "Veloster"),
        "Jeep" to listOf("Renegade", "Compass", "Cherokee", "Grand Cherokee", "Wrangler", "Gladiator", "Avenger", "Commander"),
        "Kia" to listOf("Picanto", "Rio", "Stonic", "Ceed", "ProCeed", "XCeed", "Sportage", "Sorento", "Soul EV", "EV6", "EV9", "Niro", "Stinger", "Optima"),
        "Land Rover" to listOf("Range Rover", "Range Rover Sport", "Range Rover Velar", "Range Rover Evoque", "Discovery", "Discovery Sport", "Defender", "Freelander"),
        "Lexus" to listOf("CT", "IS", "ES", "LS", "RC", "LC", "UX", "NX", "RX", "RZ", "LM", "GX", "LX"),
        "Mazda" to listOf("Mazda2", "Mazda3", "Mazda6", "MX-5", "MX-30", "CX-3", "CX-5", "CX-30", "CX-60", "CX-80", "CX-90", "RX-8"),
        "Mercedes-Benz" to listOf("Clase A", "Clase B", "Clase C", "Clase E", "Clase S", "CLA", "CLS", "GLA", "GLB", "GLC", "GLE", "GLS", "G (Geländewagen)", "EQE", "EQS", "EQB", "EQA", "Clase V", "AMG GT", "SL"),
        "Mini" to listOf("Cooper (3 puertas)", "Cooper 5p", "Clubman", "Countryman", "Convertible", "John Cooper Works", "Mini Electric", "Aceman"),
        "Mitsubishi" to listOf("Space Star", "ASX", "Eclipse Cross", "Outlander", "Pajero", "L200", "Colt", "i-MiEV", "Lancer", "Carisma"),
        "Nissan" to listOf("Micra", "Juke", "Qashqai", "X-Trail", "Ariya", "Leaf", "Navara", "GT-R", "Z (400Z)", "Pathfinder", "Note", "Pulsar", "370Z"),
        "Opel" to listOf("Corsa", "Astra", "Insignia", "Crossland", "Grandland", "Mokka", "Combo", "Vivaro", "Movano", "Zafira", "Adam", "Karl Rocks", "Rocks Electric"),
        "Peugeot" to listOf("108", "208", "2008", "308", "3008", "508", "5008", "408", "Rifter", "Partner", "Traveller", "Expert", "Boxer", "e-208", "e-2008", "e-308", "e-Rifter"),
        "Porsche" to listOf("Macan", "Cayenne", "Cayenne Coupé", "Panamera", "Taycan", "911", "718 Boxster", "718 Cayman", "Carrera GT"),
        "Renault" to listOf("Twingo", "Clio", "Zoe", "Megane", "Megane E-Tech", "Scenic", "Captur", "Kadjar", "Austral", "Rafale", "Arkana", "Koleos", "Espace", "Trafic", "Master", "Kangoo", "5 E-Tech"),
        "Seat" to listOf("Ibiza", "Leon", "Arona", "Ateca", "Tarraco", "Alhambra", "Mii", "Toledo", "Exeo", "Altea"),
        "Škoda" to listOf("Fabia", "Scala", "Octavia", "Superb", "Kamiq", "Karoq", "Kodiaq", "Enyaq iV", "Enyaq Coupé iV", "Elaq", "Citigo", "Rapid"),
        "Subaru" to listOf("Impreza", "WRX", "XV", "Crosstrek", "Forester", "Outback", "Levorg", "BRZ", "Solterra", "Legacy", "Tribeca"),
        "Suzuki" to listOf("Swift", "Swift Sport", "Baleno", "Ignis", "S-Cross", "Vitara", "Jimny", "Across", "Swace", "Splash", "Alto"),
        "Tesla" to listOf("Model 3", "Model Y", "Model S", "Model X", "Roadster", "Cybertruck"),
        "Toyota" to listOf("Aygo", "Aygo X", "Yaris", "Yaris Cross", "Corolla", "Corolla Cross", "Prius", "C-HR", "bZ4X", "Rav4", "Highlander", "Land Cruiser", "Hilux", "Proace", "Supra", "GR86", "Mirai", "Camry", "Celica", "MR2"),
        "Volkswagen" to listOf("Polo", "Golf", "ID.3", "ID.4", "ID.5", "ID.7", "ID. Buzz", "T-Cross", "T-Roc", "Taigo", "Tiguan", "Touareg", "Passat", "Arteon", "Scirocco", "Beetle", "Sharan", "Touran", "Caddy", "Multivan", "Amarok", "Lupo", "Up!"),
        "Volvo" to listOf("XC40", "C40", "XC60", "XC90", "S60", "S90", "V60", "V90", "EX30", "EX90", "C30", "S40", "V40", "V50", "850"),
    )

    val MARCAS = TODAS.keys.toList().sorted()

    fun modelosDe(marca: String): List<String> = TODAS[marca] ?: emptyList()
}
