package dev.proplayer919.konstruct.bot;

import java.util.Collection;
import java.util.Set;

public class UsernameGenerator {
    private final static String[] adjectives = {
            "Able", "Active", "Alert", "Ambitious", "Amused", "Artistic", "Attentive",
            "Authentic", "Beautiful", "Benevolent", "Bold", "Brave", "Brilliant", "Calm", "Careful", "Caring",
            "Charming", "Cheerful", "Clever", "Compassionate", "Confident", "Considerate", "Content", "Creative",
            "Curious", "Daring", "Dedicated", "Dependable", "Determined", "Diligent", "Dynamic", "Eager", "Easygoing",
            "Efficient", "Elegant", "Empathetic", "Energetic", "Enthusiastic", "Ethical", "Experienced", "Fair",
            "Faithful", "Fearless", "Focused", "Forgiving", "Friendly", "Funny", "Generous", "Gentle", "Genuine",
            "Graceful", "Grateful", "Hardworking", "Harmonious", "Helpful", "Honest", "Hopeful", "Humble", "Imaginative",
            "Impartial", "Independent", "Innovative", "Insightful", "Inspiring", "Intelligent", "Intuitive", "Inventive",
            "Joyful", "Kind", "Lively", "Logical", "Loyal", "Loving", "Mindful", "Motivated", "Noble",
            "Observant", "Optimistic", "Organized", "Original", "Outgoing", "Passionate", "Patient",
            "Peaceful", "Perceptive", "Persistent", "Philosophical", "Playful", "Polite", "Positive", "Powerful",
            "Practical", "Proactive", "Productive", "Protective", "Proud", "Punctual", "Quiet", "Rational", "Realistic",
            "Reliable", "Resilient", "Respectful", "Responsible", "Resourceful", "Responsive", "Sincere", "Skillful",
            "Sociable", "Spirited", "Spontaneous", "Stable", "Strategic", "Strong", "Successful", "Supportive", "Sympathetic",
            "Talented", "Thoughtful", "Tolerant", "Trustworthy", "Unique", "Upbeat", "Versatile",
            "Vibrant", "Vigilant", "Warm", "Wise", "Witty", "Youthful", "Zealous", "Zesty"
    };

    private final static String[] nouns = {
            "Aaron", "Abby", "Adam", "Alex", "Alice", "Andrew", "Anna", "Anthony", "Aria", "Arthur",
            "Bella", "Ben", "Blake", "Brian", "Caitlyn", "Caleb", "Carla", "Carlos", "Carter", "Chloe",
            "David", "Daisy", "Daniel", "Derek", "Diana", "Dylan", "Eleanor", "Elijah", "Ella", "Emma",
            "Ethan", "Evelyn", "Finn", "Fiona", "Frank", "Gabriel", "Grace", "Hannah", "Harper", "Henry",
            "Isabella", "Isaac", "Ivy", "Jack", "Jacob", "James", "Jane", "Jasmine", "Jason", "Jenna",
            "Jessica", "John", "Jonathan", "Jordan", "Joseph", "Julia", "Justin", "Kaitlyn", "Kara", "Katie",
            "Kevin", "Liam", "Lily", "Logan", "Lucas", "Lucy", "Mason", "Madison", "Maggie", "Maria",
            "Matthew", "Megan", "Michael", "Mila", "Nathan", "Natalie", "Nicholas", "Noah", "Olivia", "Oscar",
            "Owen", "Paige", "Patrick", "Penelope", "Peter", "Phoebe", "Rachel", "Rebecca", "Richard", "Riley",
            "Robert", "Ryan", "Samuel", "Sarah", "Sophie", "Stephanie", "Thomas", "Tristan", "Tyler", "Victoria",
            "Violet", "William", "Zachary", "Zoe", "Bear", "Beagle", "Bulldog", "Cheetah", "Chimpanzee", "Coyote",
            "Deer", "Dolphin", "Duck", "Eagle", "Elephant", "Falcon", "Fox", "Frog", "Giraffe", "Goat",
            "Hawk", "Hippo", "Horse", "Jaguar", "Kangaroo", "Koala", "Leopard", "Lion", "Llama", "Monkey",
            "Moose", "Otter", "Owl", "Panda", "Parrot", "Penguin", "Rabbit", "Raccoon", "Seal", "Shark",
            "Sheep", "Sloth", "Sparrow", "Tiger", "Turtle", "Walrus", "Whale", "Wolf", "Zebra"
    };

    private static String generateUsernameRaw() {
        String adjective = adjectives[(int) (Math.random() * adjectives.length)];
        String noun = nouns[(int) (Math.random() * nouns.length)];
        int number = (int) (Math.random() * 900) + 100;
        return adjective + noun + number;
    }

    public static String generateUsername() {
        String username;
        do {
            username = generateUsernameRaw();
        } while (username.length() > 16);
        return username;
    }

    public static String generateUniqueUsername(Collection<String> existingUsernames) {
        String username;
        do {
            username = generateUsername();
        } while (existingUsernames.contains(username));
        return username;
    }
}
