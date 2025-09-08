package com.example.mentalwellnessbot.service;

import com.example.mentalwellnessbot.model.Challenge;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FlowService
 * - Provides: intro(), mainMenu(), flow(chatId, challenge) to start, and flow(chatId, challenge, day) to show Day N.
 * - Each step includes buttons: Watch Video | Read PDF | Download App | How to Practice | Next Day | Back to Menu
 * - Day 21 shows completion & reminder CTA.
 *
 * Callback keys your bot should handle:
 *   CHALLENGE:<NAME>
 *   FLOW:NEXT:<CHALLENGE>:<DAY>
 *   MENU:OPEN
 *   REMIND:START:<CHALLENGE>:<DAYS>
 */
@Service
public class FlowService {

    // ======= PUBLIC API =======

    /** Intro + main menu */
    public List<SendMessage> intro(String chatId) {
        String text =
                "👋 Hi, I’m your Mental Wellness Assistant.\n\n" +
                "I can guide you through simple, science-backed tools to improve your mental well-being.\n\n" +
                "Which challenge would you like to work on today?";
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(mainMenu())
                .build();
        return List.of(msg);
    }

    /** Challenge list */
    public InlineKeyboardMarkup mainMenu() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(Arrays.asList(btn("🧠 Overthinking", "CHALLENGE:OVERTHINKING"), btn("😰 Anxiety", "CHALLENGE:ANXIETY")));
        rows.add(Arrays.asList(btn("😞 Low Self-Esteem", "CHALLENGE:LOW_SELF_ESTEEM"), btn("😔 Depression", "CHALLENGE:DEPRESSION")));
        rows.add(Arrays.asList(btn("📱 Social Comparison", "CHALLENGE:SOCIAL_COMPARISON"), btn("🫂 Loneliness", "CHALLENGE:LONELINESS")));
        rows.add(Arrays.asList(btn("🔥 Burnout", "CHALLENGE:BURNOUT"), btn("💢 Stress", "CHALLENGE:STRESS")));
        return new InlineKeyboardMarkup(rows);
    }

    /** Start a challenge at Day 1 (also shows the short 1–2 line intro for the topic). */
    public List<SendMessage> flow(String chatId, Challenge c) {
        List<SendMessage> out = new ArrayList<>();
        out.add(introForChallenge(chatId, c));
        out.addAll(flow(chatId, c, 1));
        return out;
    }

    /** Show Day N of a challenge (1..21). */
    public List<SendMessage> flow(String chatId, Challenge c, int day) {
        int clamped = Math.max(1, Math.min(21, day));
        Step step = FLOWS.get(c).get(clamped - 1);
        return List.of(buildStepMessage(chatId, c, step));
    }

    // ======= INTERNAL MODEL =======

    private static final class Step {
        final int day;
        final String title;
        final String oneLiner;  // 1–2 lines brief guidance
        final String videoUrl;
        final String pdfUrl;
        final String appUrl;      // Can be empty if not applicable
        final String practiceUrl; // Can be empty if not applicable

        Step(int day, String title, String oneLiner, String videoUrl, String pdfUrl, String appUrl, String practiceUrl) {
            this.day = day;
            this.title = title;
            this.oneLiner = oneLiner;
            this.videoUrl = videoUrl;
            this.pdfUrl = pdfUrl;
            this.appUrl = appUrl;
            this.practiceUrl = practiceUrl;
        }

        static Step of(int day, String title, String oneLiner, String videoUrl, String pdfUrl, String appUrl, String practiceUrl) {
            return new Step(day, title, oneLiner, videoUrl, pdfUrl, appUrl, practiceUrl);
        }
    }

    // ======= LINKS (centralized so you can change once) =======

    // Apps
    private static final String APP_GOOGLE_FIT     = "https://play.google.com/store/apps/details?id=com.google.android.apps.fitness";
    private static final String APP_6000_THOUGHTS  = "https://play.google.com/store/apps/details?id=com.slftok.sixthousandthoughts";
    private static final String APP_GRATITUDE      = "https://play.google.com/store/apps/details?id=com.northstar.gratitude";
    private static final String APP_BREATHE        = "https://play.google.com/store/apps/details?id=com.havabee.breathe";
    private static final String APP_HEARTFULNESS   = "https://play.google.com/store/apps/details?id=org.heartfulness.heartintune.prod";

    // Practice (Instagram guides)
    private static final String HOW_TO_JOURNAL     = "https://www.instagram.com/reel/DMm1TCephmY/?igsh=dWtqNzlucXFvazVt";
    private static final String HOW_TO_BREATHING   = "https://www.instagram.com/reel/DMmZtB-JDC3/?igsh=andoM3Jobnl3eTgz";
    private static final String HOW_TO_MEDITATE    = "https://www.instagram.com/p/DMm4IUxJW7u/?igsh=OGR0amUwbDIwYTQz";
    private static final String HOW_TO_GRATITUDE   = "https://www.instagram.com/reel/DLkbzM5JWYw/?igsh=MWg5bGN1MXU5YnBiYw==";

    // Videos (YouTube)
    private static final String VID_OVERTHINKING   = "https://youtu.be/re842jZmpZg?si=JPRp2xkxbpzT_V8_";
    private static final String VID_ANXIETY_1      = "https://youtu.be/ijXBRQJy7nM?si=Fi8nelqzbU-l5mI5";
    private static final String VID_ANXIETY_2      = "https://youtu.be/xbOpIAtxNuA?si=gPTL1uvRrb31cYTG";
    private static final String VID_BURNOUT        = "https://youtu.be/qxuTLJ9rtiI?si=jJDTrnKMNJKyfwh5";
    private static final String VID_DEPRESSION     = "https://youtu.be/xbOpIAtxNuA?si=GrJ9znsK9KQF9swR";
    private static final String VID_SOCIAL_COMP    = "https://youtu.be/SC2jARXp9mc?si=CAI0m6GW625-bM4i";
    private static final String VID_LONELINESS     = "https://youtu.be/OYhdS_gIpbg?si=-jhqYEcADJZQVr6M";
    private static final String VID_LOW_SE_1       = "https://youtu.be/BKf_CX6OLLs?si=L91j2is6D3CV9SCx";
    private static final String VID_LOW_SE_2       = "https://youtu.be/-A7HaGjn_zY?si=S2UxEtOJpZlZOcwl";
    private static final String VID_STRESS         = "https://youtu.be/0nzUCn7n12U?si=zBhV-HC1c8PYJ3y5";

    // PDFs (Drive)
    private static final String PDF_OVERTHINKING   = "https://drive.google.com/file/d/1DFF42zBwg6LFJoRceclzO59KOWa3-kOd/view";
    private static final String PDF_ANXIETY        = "https://drive.google.com/file/d/1DysLI7iEDzoXSMyP6Yn6YM_AEltUQ3jZ/view";
    private static final String PDF_BURNOUT        = "https://drive.google.com/file/d/1Dw-VqeJNEikBmla_VSqDAdc2ziIZsncO/view";
    private static final String PDF_DEPRESSION     = "https://drive.google.com/file/d/1DiNsADRmPLdrCd14eHo8pAuGaHacfo8w/view";
    private static final String PDF_SOCIAL_COMP    = "https://drive.google.com/file/d/1DOsNZAclcWF83R2-i3l1-txpbUZFOWT8/view";
    private static final String PDF_LONELINESS     = "https://drive.google.com/file/d/1Dcygx3Bry6or38s9FTr5Cpku2C9vmX_G/view";
    private static final String PDF_LOW_SE         = "https://drive.google.com/file/d/1ECm9gVsWyRmMEvj0UMILSd12kuy06YTN/view";
    private static final String PDF_STRESS         = "https://drive.google.com/file/d/1EEGoPaG_ldtji6XJaB69c73oFCwiC5la/view";

    // ======= FLOWS =======

    private static final Map<Challenge, List<Step>> FLOWS = new EnumMap<>(Challenge.class);

    static {
        FLOWS.put(Challenge.OVERTHINKING, buildOverthinking());
        FLOWS.put(Challenge.ANXIETY, buildAnxiety());
        FLOWS.put(Challenge.LOW_SELF_ESTEEM, buildLowSelfEsteem());
        FLOWS.put(Challenge.DEPRESSION, buildDepression());
        FLOWS.put(Challenge.SOCIAL_COMPARISON, buildSocialComparison());
        FLOWS.put(Challenge.LONELINESS, buildLoneliness());
        FLOWS.put(Challenge.BURNOUT, buildBurnout());
        FLOWS.put(Challenge.STRESS, buildStress());
    }

    private static List<Step> buildOverthinking() {
        List<Step> s = new ArrayList<>();
        // rotate journaling, breathing, walk, meditation, reframing
        s.add(Step.of(1,  "Thought Journaling (Kickoff)", "Empty your mind onto paper for 10 mins to break the loop.", VID_OVERTHINKING, PDF_OVERTHINKING, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(2,  "Breathing Reset (Box 4-4-4-4)", "Slow breathing lowers mental noise and restores calm.", VID_ANXIETY_1, PDF_OVERTHINKING, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(3,  "Clarity Walk", "A 15-min walk—no phone—just observe sights & sounds.", VID_STRESS, PDF_OVERTHINKING, APP_GOOGLE_FIT, ""));
        s.add(Step.of(4,  "Journaling: Facts vs Stories", "Write the situation, list facts, then stories—separate both.", VID_OVERTHINKING, PDF_OVERTHINKING, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(5,  "Meditation (10 min)", "Sit, breathe, notice thoughts pass like clouds—no chasing.", VID_ANXIETY_1, PDF_OVERTHINKING, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(6,  "Worry Window", "Schedule 10 mins to worry later; redirect mind until then.", VID_OVERTHINKING, PDF_OVERTHINKING, "", ""));
        s.add(Step.of(7,  "Reframing: ‘What’s in my control?’", "List actions you can take; release what you can’t.", VID_OVERTHINKING, PDF_OVERTHINKING, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(8,  "Breathing Ladders", "Exhale slightly longer than inhale to relax the brain.", VID_ANXIETY_1, PDF_OVERTHINKING, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(9,  "Digital Pause", "Pick 2 hours today as phone-free focus time.", VID_STRESS, PDF_OVERTHINKING, "", ""));
        s.add(Step.of(10, "Journaling: 3 Solutions", "Write 3 tiny next steps for a current worry.", VID_OVERTHINKING, PDF_OVERTHINKING, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(11, "Meditation (Focus on Sounds)", "Gently anchor attention to ambient sounds.", VID_ANXIETY_1, PDF_OVERTHINKING, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(12, "Walk & Labeling", "Walk and label: ‘seeing, hearing, breathing’ to ground.", VID_STRESS, PDF_OVERTHINKING, APP_GOOGLE_FIT, ""));
        s.add(Step.of(13, "Journaling: Best Friend Lens", "Write advice you’d give a friend in your shoes.", VID_OVERTHINKING, PDF_OVERTHINKING, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(14, "Breathing (4-7-8)", "Inhale 4, hold 7, exhale 8—repeat 4 cycles.", VID_ANXIETY_1, PDF_OVERTHINKING, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(15, "Focus Sprint (25 min)", "One task, timer on—then short break. Repeat once.", VID_STRESS, PDF_OVERTHINKING, "", ""));
        s.add(Step.of(16, "Journaling: Evidence Check", "Challenge ‘what ifs’ with evidence for/against.", VID_OVERTHINKING, PDF_OVERTHINKING, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(17, "Meditation (Body Scan)", "Scan head-to-toe, soften tight areas.", VID_ANXIETY_1, PDF_OVERTHINKING, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(18, "Gratitude x3", "Note 3 things that went okay today.", VID_LOW_SE_1, PDF_OVERTHINKING, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(19, "Breathing (5-5-5)", "Inhale 5, hold 5, exhale 5—steady rhythm.", VID_ANXIETY_1, PDF_OVERTHINKING, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(20, "Journaling: ‘Next Right Thing’", "Pick one tiny action; do it now.", VID_OVERTHINKING, PDF_OVERTHINKING, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(21, "Reflection & Plan", "What worked best? Keep 2 tools as daily habits.", VID_OVERTHINKING, PDF_OVERTHINKING, "", ""));
        return s;
    }

    private static List<Step> buildAnxiety() {
        List<Step> s = new ArrayList<>();
        s.add(Step.of(1,  "Breathing (Box 4-4-4-4)", "Steady your system with 4s inhale/hold/exhale.", VID_ANXIETY_1, PDF_ANXIETY, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(2,  "Walk in Daylight", "10–20 mins outside—notice light, colors, temperature.", VID_STRESS, PDF_ANXIETY, APP_GOOGLE_FIT, ""));
        s.add(Step.of(3,  "Thought Journaling", "Name the worry, write it out—reduce the charge.", VID_ANXIETY_2, PDF_ANXIETY, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(4,  "Meditation (10 min)", "Sit, breathe, let thoughts pass—no fighting them.", VID_ANXIETY_1, PDF_ANXIETY, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(5,  "Grounding 5-4-3-2-1", "Name 5 see / 4 touch / 3 hear / 2 smell / 1 taste.", VID_ANXIETY_2, PDF_ANXIETY, "", ""));
        s.add(Step.of(6,  "Breathing (4-7-8)", "Calm the nervous system with a longer exhale.", VID_ANXIETY_1, PDF_ANXIETY, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(7,  "Caffeine Audit", "Reduce caffeine after noon; note effect on anxiety.", VID_STRESS, PDF_ANXIETY, "", ""));
        s.add(Step.of(8,  "Journaling: Catastrophe vs Likely", "List likely outcomes; rate from 1–10.", VID_ANXIETY_2, PDF_ANXIETY, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(9,  "Light Cardio", "5–10 min brisk walk or stairs—burn off adrenaline.", VID_STRESS, PDF_ANXIETY, APP_GOOGLE_FIT, ""));
        s.add(Step.of(10, "Meditation (Body Scan)", "Scan body, soften tension on exhale.", VID_ANXIETY_1, PDF_ANXIETY, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(11, "Breathing (Physiological Sigh)", "Two short inhales + long exhale x 5.", VID_ANXIETY_1, PDF_ANXIETY, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(12, "Journaling: Reassurance Bank", "Write 3 times you handled hard things.", VID_ANXIETY_2, PDF_ANXIETY, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(13, "Phone-Free Hour", "Protect one hour. Read, cook, or stretch.", VID_STRESS, PDF_ANXIETY, "", ""));
        s.add(Step.of(14, "Gratitude x3", "Notice small wins; anxiety shrinks with appreciation.", VID_LOW_SE_1, PDF_ANXIETY, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(15, "Breathing Ladder", "Exhale 1–2s longer than inhale. Repeat 5 mins.", VID_ANXIETY_1, PDF_ANXIETY, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(16, "Meditation (Sounds Anchor)", "Rest attention on ambient sounds today.", VID_ANXIETY_1, PDF_ANXIETY, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(17, "Journaling: ‘If… then I’ll…’", "Plan one coping action for a feared scenario.", VID_ANXIETY_2, PDF_ANXIETY, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(18, "Gentle Stretch", "5–8 mins shoulder/neck/back looseners.", VID_STRESS, PDF_ANXIETY, "", ""));
        s.add(Step.of(19, "Breathing (5-5-5)", "Keep rhythm steady; let the mind settle.", VID_ANXIETY_1, PDF_ANXIETY, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(20, "Meditation (Gratitude)", "Bring to mind someone/something you value.", VID_ANXIETY_1, PDF_ANXIETY, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(21, "Reflection & Plan", "Pick the 2 tools you’ll keep daily.", VID_ANXIETY_2, PDF_ANXIETY, "", ""));
        return s;
    }

    private static List<Step> buildLowSelfEsteem() {
        List<Step> s = new ArrayList<>();
        s.add(Step.of(1,  "Affirmations (Start)", "Repeat 3 kind truths about yourself, morning & night.", VID_LOW_SE_1, PDF_LOW_SE, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(2,  "Small Win Journal", "Write 3 small wins from today.", VID_LOW_SE_2, PDF_LOW_SE, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(3,  "Posture & Breath", "Stand tall, slow breath—body signals confidence.", VID_ANXIETY_1, PDF_LOW_SE, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(4,  "Strengths List", "List 5 strengths others note about you.", VID_LOW_SE_1, PDF_LOW_SE, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(5,  "Gratitude for Self", "Write 3 things you appreciate about yourself.", VID_LOW_SE_1, PDF_LOW_SE, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(6,  "Affirmations (Mirror)", "Speak your affirmations while looking in the mirror.", VID_LOW_SE_2, PDF_LOW_SE, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(7,  "Values Check", "Name your top 3 values; do one action for each.", VID_LOW_SE_1, PDF_LOW_SE, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(8,  "Breathing (4-7-8)", "Settle nerves before challenges today.", VID_ANXIETY_1, PDF_LOW_SE, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(9,  "Self-Compassion Letter", "Write to yourself like a kind friend.", VID_LOW_SE_1, PDF_LOW_SE, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(10, "Skill Micro-Step", "Practice a skill for 10 mins—consistency builds esteem.", VID_STRESS, PDF_LOW_SE, "", ""));
        s.add(Step.of(11, "Affirmations (Identity)", "Start with ‘I am someone who…’ and finish positively.", VID_LOW_SE_2, PDF_LOW_SE, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(12, "Gratitude x3", "Train your brain to spot the good.", VID_LOW_SE_1, PDF_LOW_SE, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(13, "Boundaries Note", "Write one ‘No’ you’ll say to protect your time.", VID_LOW_SE_1, PDF_LOW_SE, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(14, "Breathing (Box 4-4-4-4)", "Steady breath before difficult conversations.", VID_ANXIETY_1, PDF_LOW_SE, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(15, "Kind Act", "Do one small kind act for someone (or yourself).", VID_LOW_SE_1, PDF_LOW_SE, "", ""));
        s.add(Step.of(16, "Reframe Self-Talk", "Replace ‘I can’t’ with ‘I’m learning to’.", VID_LOW_SE_2, PDF_LOW_SE, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(17, "Meditation (Loving-Kindness)", "Send well-wishes to yourself and others.", VID_ANXIETY_1, PDF_LOW_SE, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(18, "Win Wall", "Write 5 achievements—big or tiny—this month.", VID_LOW_SE_1, PDF_LOW_SE, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(19, "Affirmations (Future-You)", "Speak as the version of you you’re becoming.", VID_LOW_SE_2, PDF_LOW_SE, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(20, "Share a Strength", "Use a strength to help someone today.", VID_LOW_SE_1, PDF_LOW_SE, "", ""));
        s.add(Step.of(21, "Reflection & Plan", "Keep 2 practices daily for ongoing confidence.", VID_LOW_SE_1, PDF_LOW_SE, "", ""));
        return s;
    }

    private static List<Step> buildDepression() {
        List<Step> s = new ArrayList<>();
        s.add(Step.of(1,  "Gentle Start: Sunlight Walk", "10-15 min in daylight can lift mood.", VID_DEPRESSION, PDF_DEPRESSION, APP_GOOGLE_FIT, ""));
        s.add(Step.of(2,  "Gratitude x3", "Note 3 small good things—train attention to the good.", VID_LOW_SE_1, PDF_DEPRESSION, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(3,  "Breathing (4-7-8)", "Soothe body first; mind follows.", VID_ANXIETY_1, PDF_DEPRESSION, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(4,  "Reach Out", "Message someone you trust; connection helps healing.", VID_DEPRESSION, PDF_DEPRESSION, "", ""));
        s.add(Step.of(5,  "Tiny Chores", "Do one 5-min task: dishes, desk, or laundry.", VID_STRESS, PDF_DEPRESSION, "", ""));
        s.add(Step.of(6,  "Journaling: One Step", "Write one realistic step for today.", VID_DEPRESSION, PDF_DEPRESSION, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(7,  "Sun & Steps", "Aim for 3K–5K steps today.", VID_STRESS, PDF_DEPRESSION, APP_GOOGLE_FIT, ""));
        s.add(Step.of(8,  "Meditation (Breath)", "5–10 mins—gentle attention to inhale/exhale.", VID_ANXIETY_1, PDF_DEPRESSION, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(9,  "Compassion Note", "Write one kind sentence to yourself.", VID_DEPRESSION, PDF_DEPRESSION, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(10, "Nourish", "Have a balanced meal & hydrate well.", VID_STRESS, PDF_DEPRESSION, "", ""));
        s.add(Step.of(11, "Gratitude for People", "Thank someone or note why they matter.", VID_LOW_SE_1, PDF_DEPRESSION, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(12, "Light Movement", "Stretch or short walk after lunch.", VID_STRESS, PDF_DEPRESSION, APP_GOOGLE_FIT, ""));
        s.add(Step.of(13, "Breathing (Box 4-4-4-4)", "Reset tension with steady rhythm.", VID_ANXIETY_1, PDF_DEPRESSION, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(14, "Journaling: Win List", "List 5 things you did despite low mood.", VID_DEPRESSION, PDF_DEPRESSION, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(15, "Media Diet", "Reduce doomscrolling; replace with music or nature.", VID_STRESS, PDF_DEPRESSION, "", ""));
        s.add(Step.of(16, "Meditation (Body Scan)", "Soften jaw, shoulders, belly.", VID_ANXIETY_1, PDF_DEPRESSION, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(17, "Walk with a Friend", "Move + connect = double benefits.", VID_DEPRESSION, PDF_DEPRESSION, APP_GOOGLE_FIT, ""));
        s.add(Step.of(18, "Gratitude x3 (Evening)", "Reflect on small sparks of okay-ness.", VID_LOW_SE_1, PDF_DEPRESSION, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(19, "Breathing (5-5-5)", "Calm rhythm before sleep.", VID_ANXIETY_1, PDF_DEPRESSION, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(20, "Journaling: Plan Tomorrow", "One doable plan for the morning.", VID_DEPRESSION, PDF_DEPRESSION, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(21, "Reflection & Next Steps", "Keep 2–3 tools you liked most.", VID_DEPRESSION, PDF_DEPRESSION, "", ""));
        return s;
    }

    private static List<Step> buildSocialComparison() {
        List<Step> s = new ArrayList<>();
        s.add(Step.of(1,  "Gratitude Lens", "Write 3 good things—shift focus from others to your path.", VID_SOCIAL_COMP, PDF_SOCIAL_COMP, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(2,  "Unfollow/ Mute Audit", "Curate feeds: remove accounts that trigger comparison.", VID_SOCIAL_COMP, PDF_SOCIAL_COMP, "", ""));
        s.add(Step.of(3,  "Walk & Reflect", "10-15 min walk: what matters to you today?", VID_STRESS, PDF_SOCIAL_COMP, APP_GOOGLE_FIT, ""));
        s.add(Step.of(4,  "Affirmations (Self-Worth)", "Repeat 3 statements of your unique value.", VID_LOW_SE_1, PDF_SOCIAL_COMP, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(5,  "Journaling: My Metrics", "Define your success metrics—personal & realistic.", VID_SOCIAL_COMP, PDF_SOCIAL_COMP, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(6,  "Breathing (4-7-8)", "Calm comparison spikes with breath.", VID_ANXIETY_1, PDF_SOCIAL_COMP, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(7,  "Skill Micro-Practice", "10 mins on a skill you care about.", VID_STRESS, PDF_SOCIAL_COMP, "", ""));
        s.add(Step.of(8,  "Gratitude for People", "Appreciate someone who supports you.", VID_LOW_SE_1, PDF_SOCIAL_COMP, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(9,  "Meditation (Loving-Kindness)", "Well-wishes for you & others—less envy, more peace.", VID_ANXIETY_1, PDF_SOCIAL_COMP, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(10, "Journaling: Progress Log", "Write 3 ways you’ve grown this month.", VID_SOCIAL_COMP, PDF_SOCIAL_COMP, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(11, "Phone-Free Hour", "Protect attention. Create instead of compare.", VID_STRESS, PDF_SOCIAL_COMP, "", ""));
        s.add(Step.of(12, "Breathing (Box 4-4-4-4)", "Steady your system when triggers appear.", VID_ANXIETY_1, PDF_SOCIAL_COMP, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(13, "Affirmations (Identity)", "‘I’m someone who shows up for myself.’", VID_LOW_SE_2, PDF_SOCIAL_COMP, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(14, "Walk with Music/Podcast", "Fill your mind with nourishing input.", VID_STRESS, PDF_SOCIAL_COMP, APP_GOOGLE_FIT, ""));
        s.add(Step.of(15, "Journaling: My Lane", "Write 5 reasons your path is yours alone.", VID_SOCIAL_COMP, PDF_SOCIAL_COMP, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(16, "Meditation (Breath)", "Return to breath whenever comparison thoughts arise.", VID_ANXIETY_1, PDF_SOCIAL_COMP, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(17, "Gratitude x3 (Evening)", "Close the day on your wins.", VID_LOW_SE_1, PDF_SOCIAL_COMP, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(18, "Breathing Ladder", "Exhale longer to downshift quickly.", VID_ANXIETY_1, PDF_SOCIAL_COMP, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(19, "Create & Share", "Post or share something genuine (or keep it private).", VID_SOCIAL_COMP, PDF_SOCIAL_COMP, "", ""));
        s.add(Step.of(20, "Kind Note to Self", "Write a kind message to future-you.", VID_SOCIAL_COMP, PDF_SOCIAL_COMP, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(21, "Reflection & Plan", "Choose 2 tools to keep as habits.", VID_SOCIAL_COMP, PDF_SOCIAL_COMP, "", ""));
        return s;
    }

    private static List<Step> buildLoneliness() {
        List<Step> s = new ArrayList<>();
        s.add(Step.of(1,  "Gratitude x3", "Notice what’s present & supportive in life.", VID_LONELINESS, PDF_LONELINESS, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(2,  "Walk in Fresh Air", "10-15 min noticing nature & people around.", VID_STRESS, PDF_LONELINESS, APP_GOOGLE_FIT, ""));
        s.add(Step.of(3,  "Breathing (4-7-8)", "Soothe anxious solitude; reconnect inward.", VID_ANXIETY_1, PDF_LONELINESS, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(4,  "Reach Out", "Text/call one person—share a small update.", VID_LONELINESS, PDF_LONELINESS, "", ""));
        s.add(Step.of(5,  "Journaling: Connection Map", "List 5 people; plan one small touchpoint.", VID_LONELINESS, PDF_LONELINESS, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(6,  "Meditation (Loving-Kindness)", "Wish well to yourself & others.", VID_ANXIETY_1, PDF_LONELINESS, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(7,  "Community Moment", "Join a group/class or say hi to a neighbor.", VID_LONELINESS, PDF_LONELINESS, "", ""));
        s.add(Step.of(8,  "Gratitude for People", "Note why 2 people matter to you.", VID_LOW_SE_1, PDF_LONELINESS, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(9,  "Walk & Smile", "Smile at 3 people; notice how it feels.", VID_STRESS, PDF_LONELINESS, APP_GOOGLE_FIT, ""));
        s.add(Step.of(10, "Breathing (Box 4-4-4-4)", "Gently regulate during lonely spikes.", VID_ANXIETY_1, PDF_LONELINESS, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(11, "Journaling: Self-Compassion", "Offer yourself kind words on paper.", VID_LONELINESS, PDF_LONELINESS, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(12, "Meditation (Body Scan)", "Relax tension; open space for connection.", VID_ANXIETY_1, PDF_LONELINESS, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(13, "Acts of Kindness", "Do 1 helpful act for someone today.", VID_LONELINESS, PDF_LONELINESS, "", ""));
        s.add(Step.of(14, "Digital Boundaries", "Reduce mindless scrolling for 2 hours.", VID_STRESS, PDF_LONELINESS, "", ""));
        s.add(Step.of(15, "Gratitude x3 (Evening)", "End your day grounded in appreciation.", VID_LOW_SE_1, PDF_LONELINESS, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(16, "Walk with a Friend", "Combine movement and connection.", VID_STRESS, PDF_LONELINESS, APP_GOOGLE_FIT, ""));
        s.add(Step.of(17, "Breathing (5-5-5)", "Soften edges of isolation with rhythm.", VID_ANXIETY_1, PDF_LONELINESS, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(18, "Journaling: Social Plan", "Map 3 small plans for the next week.", VID_LONELINESS, PDF_LONELINESS, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(19, "Meditation (Gratitude)", "Sit with appreciation—feel connection inside.", VID_ANXIETY_1, PDF_LONELINESS, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(20, "Volunteer/Aid", "Offer time/skill—connection grows by giving.", VID_LONELINESS, PDF_LONELINESS, "", ""));
        s.add(Step.of(21, "Reflection & Plan", "Choose 2 habits to keep weekly.", VID_LONELINESS, PDF_LONELINESS, "", ""));
        return s;
    }

    private static List<Step> buildBurnout() {
        List<Step> s = new ArrayList<>();
        s.add(Step.of(1,  "Meditation (10 min)", "Let your brain breathe; restore a bit of space.", VID_BURNOUT, PDF_BURNOUT, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(2,  "Gratitude x3", "Shift attention to what’s working—tiny sparks matter.", VID_LOW_SE_1, PDF_BURNOUT, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(3,  "Nature Walk", "15 mins in greenery reduces stress load.", VID_STRESS, PDF_BURNOUT, APP_GOOGLE_FIT, ""));
        s.add(Step.of(4,  "Breathing (4-7-8)", "Downshift the nervous system quickly.", VID_ANXIETY_1, PDF_BURNOUT, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(5,  "Boundaries: Stop Time", "Pick a hard stop for work today; protect rest.", VID_BURNOUT, PDF_BURNOUT, "", ""));
        s.add(Step.of(6,  "Journaling: Energy Audit", "List what drains/charges you; adjust one thing.", VID_BURNOUT, PDF_BURNOUT, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(7,  "Active Recovery", "Light movement: stretch/walk/yoga 10–15 min.", VID_STRESS, PDF_BURNOUT, APP_GOOGLE_FIT, ""));
        s.add(Step.of(8,  "Meditation (Body Scan)", "Relax tight zones; breathe into them.", VID_ANXIETY_1, PDF_BURNOUT, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(9,  "Breathing Ladder", "Exhale longer to tilt into rest mode.", VID_ANXIETY_1, PDF_BURNOUT, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(10, "Digital Sunset", "One hour tech-free before bed.", VID_STRESS, PDF_BURNOUT, "", ""));
        s.add(Step.of(11, "Gratitude for Team", "Note 2 people who support your work/life.", VID_LOW_SE_1, PDF_BURNOUT, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(12, "Walk & Reflect", "What one task truly matters today?", VID_STRESS, PDF_BURNOUT, APP_GOOGLE_FIT, ""));
        s.add(Step.of(13, "Journaling: Delegate/Delete", "List tasks to delegate, delay, delete.", VID_BURNOUT, PDF_BURNOUT, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(14, "Breathing (Box 4-4-4-4)", "Reset between meetings/work blocks.", VID_ANXIETY_1, PDF_BURNOUT, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(15, "Recovery Meal", "Eat slowly, hydrate, and step away from screens.", VID_STRESS, PDF_BURNOUT, "", ""));
        s.add(Step.of(16, "Meditation (Gratitude)", "Appreciate your effort—progress over perfection.", VID_ANXIETY_1, PDF_BURNOUT, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(17, "Active Recovery II", "Another 10–15 min movement dose.", VID_STRESS, PDF_BURNOUT, APP_GOOGLE_FIT, ""));
        s.add(Step.of(18, "Breathing (5-5-5)", "Even rhythm—calm clarity.", VID_ANXIETY_1, PDF_BURNOUT, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(19, "Journaling: Joy Sparks", "Write 3 small joys to add this week.", VID_BURNOUT, PDF_BURNOUT, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(20, "Light Social Time", "Low-effort connection with someone safe.", VID_BURNOUT, PDF_BURNOUT, "", ""));
        s.add(Step.of(21, "Reflection & Plan", "Lock 2 recovery habits into your week.", VID_BURNOUT, PDF_BURNOUT, "", ""));
        return s;
    }

    private static List<Step> buildStress() {
        List<Step> s = new ArrayList<>();
        s.add(Step.of(1,  "Breathing (Box 4-4-4-4)", "A fast reset you can use anywhere.", VID_STRESS, PDF_STRESS, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(2,  "Walk Break", "10 min brisk walk to clear stress chemicals.", VID_STRESS, PDF_STRESS, APP_GOOGLE_FIT, ""));
        s.add(Step.of(3,  "Gratitude x3", "Appreciation shifts your biology toward calm.", VID_LOW_SE_1, PDF_STRESS, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(4,  "Journaling: Brain Dump", "Unload stressors; pick one small action.", VID_OVERTHINKING, PDF_STRESS, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(5,  "Meditation (Breath)", "Slow, steady attention on inhale/exhale.", VID_ANXIETY_1, PDF_STRESS, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(6,  "Micro-Breaks", "Take 3×2-min breaks across the day.", VID_STRESS, PDF_STRESS, "", ""));
        s.add(Step.of(7,  "Breathing (4-7-8)", "Extend exhale to deepen calm.", VID_ANXIETY_1, PDF_STRESS, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(8,  "Phone-Free Hour", "Protect focus to feel less scattered.", VID_STRESS, PDF_STRESS, "", ""));
        s.add(Step.of(9,  "Walk + Sunlight", "Step outside; light anchors circadian rhythm.", VID_STRESS, PDF_STRESS, APP_GOOGLE_FIT, ""));
        s.add(Step.of(10, "Journaling: Reframe", "Ask: What’s in my control today?", VID_OVERTHINKING, PDF_STRESS, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(11, "Meditation (Body Scan)", "Loosen shoulders/neck/jaw; stress lives in muscles.", VID_ANXIETY_1, PDF_STRESS, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(12, "Breathing (5-5-5)", "Even rhythm for evening unwind.", VID_ANXIETY_1, PDF_STRESS, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(13, "Gratitude for People", "Note who helped you—send a thanks.", VID_LOW_SE_1, PDF_STRESS, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(14, "Walk & Reflect", "What actually matters this week?", VID_STRESS, PDF_STRESS, APP_GOOGLE_FIT, ""));
        s.add(Step.of(15, "Journaling: Next Right Thing", "Pick one small step and do it.", VID_OVERTHINKING, PDF_STRESS, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(16, "Meditation (Gratitude)", "Sit with appreciation, breathe it in.", VID_ANXIETY_1, PDF_STRESS, APP_HEARTFULNESS, HOW_TO_MEDITATE));
        s.add(Step.of(17, "Breathing Ladder", "Exhale slightly longer to downshift.", VID_ANXIETY_1, PDF_STRESS, APP_BREATHE, HOW_TO_BREATHING));
        s.add(Step.of(18, "Light Strength/Stretch", "2 sets body-weight / or 8-min stretch.", VID_STRESS, PDF_STRESS, "", ""));
        s.add(Step.of(19, "Gratitude x3 (Evening)", "Close the day in calm.", VID_LOW_SE_1, PDF_STRESS, APP_GRATITUDE, HOW_TO_GRATITUDE));
        s.add(Step.of(20, "Journaling: Win Wall", "List 5 things you accomplished this week.", VID_OVERTHINKING, PDF_STRESS, APP_6000_THOUGHTS, HOW_TO_JOURNAL));
        s.add(Step.of(21, "Reflection & Plan", "Keep your 2 favorite tools daily.", VID_STRESS, PDF_STRESS, "", ""));
        return s;
    }

    // ======= MESSAGES =======

    private SendMessage introForChallenge(String chatId, Challenge c) {
        String line;
        switch (c) {
            case OVERTHINKING -> line = "Overthinking can feel like mental loops. We’ll use journaling, breath, walks, and reframing to create space.";
            case ANXIETY -> line = "Anxiety is a body-first signal. We’ll steady your breath, move gently, and reframe worries into plans.";
            case LOW_SELF_ESTEEM -> line = "Low self-esteem is learned. We’ll rebuild with affirmations, small wins, and kind self-talk.";
            case DEPRESSION -> line = "Depression needs gentle consistency. We’ll use sunlight, small steps, breath, and connection.";
            case SOCIAL_COMPARISON -> line = "Comparison steals joy. We’ll shift focus to your values, progress, and gratitude.";
            case LONELINESS -> line = "Loneliness eases with connection—inside and out. We’ll build contact, kindness, and routine.";
            case BURNOUT -> line = "Burnout is chronic overload. We’ll restore with boundaries, recovery, and simple rhythms.";
            case STRESS -> line = "Stress is energy your body can clear. We’ll cycle it through with breath, movement, and focus.";
            default -> line = "Let’s begin.";
        }
        return SendMessage.builder()
                .chatId(chatId)
                .text("✅ Let’s work together on " + nice(c) + ".\n\n" + line + "\n\nI’ll guide you for 21 days. Here’s Day 1 👇")
                .build();
    }

    private SendMessage buildStepMessage(String chatId, Challenge c, Step step) {
        StringBuilder sb = new StringBuilder();
        sb.append("Day ").append(step.day).append(" — ").append(step.title).append("\n\n");
        sb.append(step.oneLiner);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Primary action buttons row
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        if (notEmpty(step.videoUrl))  row1.add(InlineKeyboardButton.builder().text("🎥 Watch Video").url(step.videoUrl).build());
        if (notEmpty(step.pdfUrl))    row1.add(InlineKeyboardButton.builder().text("📄 Read PDF").url(step.pdfUrl).build());
        rows.add(row1.stream().limit(3).collect(Collectors.toList())); // keep row neat

        // Secondary action buttons row
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        if (notEmpty(step.appUrl))        row2.add(InlineKeyboardButton.builder().text("📱 Download App").url(step.appUrl).build());
        if (notEmpty(step.practiceUrl))   row2.add(InlineKeyboardButton.builder().text("📖 How to Practice").url(step.practiceUrl).build());
        if (!row2.isEmpty()) rows.add(row2);

        // Navigation row
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        if (step.day < 21) {
            row3.add(InlineKeyboardButton.builder()
                    .text("➡ Next Day")
                    .callbackData("FLOW:NEXT:" + c.name() + ":" + (step.day + 1))
                    .build());
        } else {
            // Final day actions
            row3.add(InlineKeyboardButton.builder()
                    .text("🔁 21-Day Reminders")
                    .callbackData("REMIND:START:" + c.name() + ":21")
                    .build());
            row3.add(InlineKeyboardButton.builder()
                    .text("🗣️ Check-in")
                    .callbackData("CHALLENGE:CHECKIN:" + c.name())
                    .build());
        }
        row3.add(InlineKeyboardButton.builder().text("⬅ Back to Menu").callbackData("MENU:OPEN").build());
        rows.add(row3);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(rows);

        // If last day, append congrats line
        if (step.day == 21) {
            sb.append("\n\n🎉 You completed the 21-day ").append(nice(c)).append(" program! I’m proud of you. " +
                    "Would you like me to set reminders to keep your favorite tools?");
        }

        return SendMessage.builder()
                .chatId(chatId)
                .text(sb.toString())
                .replyMarkup(markup)
                .build();
    }

    // ======= HELPERS =======

    private InlineKeyboardButton btn(String text, String data) {
        return InlineKeyboardButton.builder().text(text).callbackData(data).build();
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.isBlank();
    }

    private static String nice(Challenge c) {
        return switch (c) {
            case OVERTHINKING -> "Overthinking";
            case ANXIETY -> "Anxiety";
            case LOW_SELF_ESTEEM -> "Low Self-Esteem";
            case DEPRESSION -> "Depression";
            case SOCIAL_COMPARISON -> "Social Comparison";
            case LONELINESS -> "Loneliness";
            case BURNOUT -> "Burnout";
            case STRESS -> "Stress";
        };
    }
}
