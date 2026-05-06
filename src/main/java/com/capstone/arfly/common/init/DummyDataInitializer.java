package com.capstone.arfly.common.init;

import com.capstone.arfly.common.domain.File;
import com.capstone.arfly.common.domain.FileType;
import com.capstone.arfly.common.repository.FileRepository;
import com.capstone.arfly.community.domain.Comment;
import com.capstone.arfly.community.domain.CommentMention;
import com.capstone.arfly.community.domain.Post;
import com.capstone.arfly.community.domain.PostImage;
import com.capstone.arfly.community.domain.PostLike;
import com.capstone.arfly.community.repository.CommentMentionRepository;
import com.capstone.arfly.community.repository.CommentRepository;
import com.capstone.arfly.community.repository.PostImageRepository;
import com.capstone.arfly.community.repository.PostLikeRepository;
import com.capstone.arfly.community.repository.PostRepository;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.domain.Role;
import com.capstone.arfly.member.domain.Terms;
import com.capstone.arfly.member.domain.UserTermsAgreement;
import com.capstone.arfly.member.repository.MemberRepository;
import com.capstone.arfly.member.repository.TermsRepository;
import com.capstone.arfly.member.repository.UserTermsAgreementRepository;
import com.capstone.arfly.notification.domain.DeviceType;
import com.capstone.arfly.notification.domain.FcmToken;
import com.capstone.arfly.notification.repository.FcmTokenRepository;
import com.capstone.arfly.pet.domain.Breeds;
import com.capstone.arfly.pet.domain.Pet;
import com.capstone.arfly.pet.domain.PetAllergy;
import com.capstone.arfly.pet.domain.Sex;
import com.capstone.arfly.pet.domain.Species;
import com.capstone.arfly.pet.repository.BreedsRepository;
import com.capstone.arfly.pet.repository.PetAllergyRepository;
import com.capstone.arfly.pet.repository.PetRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class DummyDataInitializer implements CommandLineRunner {

    private final TermsRepository termsRepository;
    private final MemberRepository memberRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final BreedsRepository breedsRepository;
    private final PetRepository petRepository;
    private final PetAllergyRepository petAllergyRepository;
    private final FileRepository fileRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final CommentMentionRepository commentMentionRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    private final Random random = new Random(42);

    private static final String[] PET_NAMES = {
        "코코", "몽이", "초코", "두부", "뽀삐", "콩이", "달이", "별이", "해피", "구름이",
        "솜이", "모찌", "쿠키", "크림이", "호두", "나비", "야옹이", "뭉치", "루나", "미미",
        "레오", "모카", "라떼", "비비", "나나", "봄이", "눈송이", "당근이", "감자", "고구마",
        "하늘이", "노을이", "복실이", "연기", "찰리", "도레미", "솔비", "달콤이", "뭉실이", "보리"
    };

    private static final String[] MEMBER_NICKNAMES = {
        "댕댕이집사", "냥이주인", "멍멍이맘", "고양이아빠", "반려동물러버",
        "산책왕", "펫스타그램", "귀여운동물", "행복한반려인", "집사생활",
        "포메라니안", "골든리트리버", "말티즈맘", "비글팸", "진도개주인",
        "페르시안집사", "러시안블루", "브리티시숏헤어", "메인쿤집사", "터키시앙고라",
        "요크셔러버", "닥스훈트맘", "시바이누", "코기팸", "사모예드주인",
        "아비시니안집사", "봄봄이주인", "달달한반려인", "뽀뽀이집사", "두부집사"
    };

    private static final String[] ALLERGIES = {
        "닭고기", "소고기", "생선", "유제품", "밀", "옥수수", "달걀", "대두", "견과류", "글루텐",
        "새우", "게", "연어", "양고기", "오리고기", "돼지고기", "쌀", "보리", "귀리", "콩"
    };

    private static final String[] POST_TITLES = {
        "우리 강아지 오늘 첫 산책 후기!", "고양이가 갑자기 밥을 안 먹어요 ㅠㅠ", "펫 보험 추천해주세요",
        "강아지 간식 추천 부탁드려요", "반려묘 털 빠짐이 너무 심해요", "강아지 훈련 어떻게 하셨나요?",
        "고양이 화장실 냄새 제거 방법", "반려견 발바닥 관리 팁 공유해요", "새로 입양한 고양이 인사드려요",
        "강아지 슬개골 수술 후기", "반려동물 여행 다녀왔어요!", "고양이 스크래처 추천해주세요",
        "강아지 피부병 어떻게 치료하셨나요?", "반려묘 중성화 후 체중 관리", "강아지 유치 빠지는 시기",
        "고양이 야식 줘도 되나요?", "반려견 목욕 주기 어떻게 해요?", "강아지 분리불안 극복 방법",
        "고양이 구토 원인이 뭘까요?", "반려동물 보험 가입하셨나요?", "강아지 눈물자국 제거법",
        "고양이 밤에 울어요 ㅠ", "반려견 심장사상충 예방약 추천", "강아지 발톱 깎기 어떻게 하세요?",
        "고양이 스트레스 받는 것 같아요", "반려동물 병원비 너무 비싸요", "강아지 노령견 관리 팁",
        "고양이 흰색 털 관리 방법", "반려견 사회화 교육 어떻게?", "강아지 먹방 보여드려요",
        "고양이 집사 일상 공유", "반려동물 사료 브랜드 추천", "강아지 배변 교육 성공했어요!",
        "고양이 목욕 시키는 방법", "반려견 놀이터 추천해주세요", "강아지 관절 건강 챙기기",
        "고양이 신장 건강 식품", "반려동물 미용 직접 해보기", "강아지 치석 관리 방법",
        "고양이 귀 청소 어떻게 하나요?", "반려견 여름 더위 대처법", "강아지 겨울철 산책 주의사항",
        "고양이 피부 알레르기 경험담", "반려동물 사진 잘 찍는 법", "강아지 입냄새 원인",
        "고양이 물 많이 마시는데 정상인가요?", "반려견 빗질 도구 추천", "강아지 똥 냄새 심해요",
        "고양이 출산 경험 공유합니다", "반려동물 간식 직접 만들기", "강아지 혼자 두면 짖어요",
        "고양이 새벽에 뛰어다녀요", "반려견 도그패드 추천", "강아지 영양제 먹이시나요?",
        "고양이 체중이 갑자기 늘었어요", "반려동물 응급처치 방법", "강아지 수염 잘라도 되나요?",
        "고양이 이가 아픈 것 같아요", "반려견 캠핑 준비물 공유", "강아지 눈 분비물이 많아요",
        "고양이 놀이 방법 추천해주세요", "반려동물 노령화 대처법", "강아지 슬픈 눈빛의 비밀",
        "고양이 킁킁거림 원인", "반려견 복종 훈련 경험담", "강아지 샴푸 추천해주세요",
        "고양이 두 마리 키우기 팁", "반려동물 건강 검진 주기", "강아지 집에서 미용하기",
        "고양이 음수량 늘리는 방법", "반려견 리드줄 추천", "강아지 발치 수술 후기",
        "고양이 코 막힘 치료법", "반려동물 사진 앨범 만들기", "강아지 귀 관리 방법",
        "고양이 털뭉치 토하는 거 정상인가요?", "반려견 비행기 탑승 후기", "강아지 피부 트러블 관리",
        "고양이 새끼 입양 후기", "반려동물 용품 추천 리스트", "강아지 겁쟁이 극복 방법",
        "고양이 브러싱 주기와 방법", "반려견 슬리커 브러시 추천", "강아지 심장 건강 챙기기",
        "고양이 수면 패턴이 이상해요", "반려동물 사진 잘 나왔어요!", "강아지 발에 혹 생겼어요",
        "고양이 화장실 위치 어디에?", "반려견 내장 기생충 예방", "강아지 겨울 옷 필요한가요?",
        "고양이 장난감 추천해주세요", "반려동물 함께 나이 들기", "강아지 귀가 빨개요",
        "고양이 눈 분비물 케어 방법", "반려견 여름 산책 시간대", "강아지 성격이 갑자기 변했어요",
        "고양이 집에서만 키우기 팁", "우리 집 냥이 소개합니다"
    };

    private static final String[] POST_CONTENTS = {
        "오늘 처음으로 산책을 나갔는데 너무 신나했어요! 다들 첫 산책 어떠셨나요?",
        "며칠 째 밥을 잘 안 먹는데 병원을 가봐야 할까요? 혹시 비슷한 경험 있으신 분 계신가요?",
        "다들 어떤 보험 쓰시나요? 추천 부탁드립니다. 비교해보고 싶어요.",
        "트릿 위주로 주고 싶은데 어떤 브랜드가 좋은지 알고 싶어요.",
        "봄이 되니까 더 심해지는 것 같아요. 다들 어떻게 관리하세요?",
        "기본 훈련부터 시작해야 할까요? 전문 트레이너를 찾는 중이에요.",
        "락다운 방향제를 써봤는데 효과가 있네요. 혹시 다른 방법도 있나요?",
        "발바닥이 건조해서 크림을 발라줬더니 좋아했어요. 여러분은 어떤 제품 쓰세요?",
        "3살 페르시안 고양이 나나입니다. 잘 부탁드려요!",
        "3개월 전에 수술을 받았는데 회복이 잘 되고 있어요. 겪어보신 분들 계신가요?",
        "제주도 여행 갔다 왔어요! 반려동물 동반 숙소 추천해드릴게요.",
        "고양이가 스크래처를 너무 좋아해서 여러 종류 써봤어요. 후기 공유합니다.",
        "피부가 빨갛고 가려워해서 병원 갔더니 알레르기래요. 식이요법 해보신 분?",
        "수술 후 2kg 쪘는데 어떻게 빼줘야 할지 모르겠어요.",
        "유치가 빠지기 시작했는데 그냥 놔둬도 되나요?",
        "야식이 습관이 되면 안 좋다고 하는데 가끔은 괜찮을까요?",
        "주 1회 목욕이 맞는지 피부 타입별로 달라지는지 궁금해요.",
        "혼자 집에 두면 이웃이 짖는다고 연락이 왔어요. 도움이 필요해요.",
        "밥 먹고 나면 꼭 한 번씩 토하는데 사료 문제인지 병원을 가봐야 할지...",
        "수의사 상담 후 보험이 훨씬 낫다고 해서 가입했어요. 나중에 후회 없었어요.",
        "눈 밑이 너무 갈색으로 물들어있는데 먹는 영양제로 효과 보신 분 계신가요?",
        "새벽 3시에 갑자기 울기 시작하는데 이유를 모르겠어요.",
        "심장사상충 예방이 중요하다는데 주사형이랑 먹는 약 중 어떤 게 좋나요?",
        "발톱을 집에서 깎으려고 하는데 무서워서 못 하겠어요. 팁 주세요!",
        "이사를 해서 그런지 밥도 안 먹고 숨어있어요. 언제쯤 적응할까요?",
        "CT 찍는 데만 50만 원이 넘었어요. 다들 어떻게 감당하시나요?",
        "노령견이 되면서 슬개골이 약해졌어요. 관절 보조제 추천해주세요.",
        "흰 고양이는 털 관리가 더 어려운 것 같아요. 특별한 팁 있나요?",
        "다른 강아지들이랑 잘 어울리지 못해서 걱정이에요. 사회화 훈련 어떻게?",
        "식욕이 엄청 좋아서 먹방 찍어봤어요! 같이 봐요~"
    };

    private static final String[] COMMENT_CONTENTS = {
        "저도 같은 경험 했어요! 정말 공감돼요.",
        "우리 애도 그런 적 있었는데 금방 나았어요 걱정 마세요~",
        "혹시 병원은 가보셨나요? 저는 바로 갔더니 좋아지던데요.",
        "너무 귀엽다ㅠㅠ 사진 더 올려주세요!",
        "저도 궁금했는데 좋은 정보 감사해요!",
        "저희도 같은 사료 먹이고 있는데 아무 문제 없었어요.",
        "수의사 선생님한테 물어보는 게 제일 좋을 것 같아요.",
        "처음엔 저도 그랬는데 시간이 지나면서 나아졌어요.",
        "오 이건 저도 시도해볼게요! 좋은 정보 감사합니다.",
        "반려동물 키우는 거 정말 힘들지만 그만큼 보람 있죠 ㅎㅎ",
        "댓글 보다가 힐링하고 가요 너무 귀여워요~",
        "저희 아이도 비슷한 증상이 있었어요. 알레르기 검사 추천드려요!",
        "처음 입양할 때부터 정기 검진 받는 게 중요한 것 같아요.",
        "정보 공유 감사해요! 저도 참고할게요.",
        "너무 공감돼요ㅠㅠ 저도 매일 고민이에요.",
        "해결됐으면 좋겠네요! 응원할게요~",
        "저는 전문 트레이너한테 맡겼더니 효과가 있었어요.",
        "사진 너무 귀엽다 하트뿅",
        "우리 동네에도 좋은 병원이 생겼으면 좋겠어요.",
        "저도 처음엔 몰랐는데 알고 나니까 너무 유용한 정보네요!",
        "반려동물 진짜 가족이잖아요. 항상 건강하길 바라요!",
        "혹시 어떤 사료 드시나요? 저도 바꿔볼까 고민 중이라서요.",
        "공감 백배입니다ㅠ 저도 같은 고민이에요.",
        "잘 해결되셨으면 좋겠어요! 힘내세요~",
        "저희는 한 달에 한 번 목욕시키는데 문제없이 지내고 있어요.",
        "너무 사랑스럽다ㅠㅠ 이름이 뭐예요?",
        "꼭 괜찮아질 거예요! 긍정적으로 생각해요.",
        "도움이 됐으면 좋겠네요. 저도 처음엔 몰랐어요~",
        "아 진짜 너무 공감돼요. 매일이 걱정이죠 ㅠ",
        "우리 애랑 친구 시켜주고 싶다ㅎㅎ 너무 귀여워요!",
        "저도 이 문제로 고생한 적 있는데, 결국 식이 조절로 해결했어요.",
        "좋은 글 감사해요! 저장해뒀다가 참고할게요.",
        "우리 아이도 똑같이 생겼어요ㅎㅎ 너무 사랑스럽죠.",
        "처음엔 걱정했는데 이제는 적응됐어요. 파이팅!",
        "혹시 어느 동네 병원 다니세요? 좋은 곳 있으면 추천 부탁드려요.",
        "정말 유익한 정보네요. 저도 바로 시도해봐야겠어요.",
        "강아지는 정말 보는 것만으로도 힐링이 되죠 ㅠㅠ",
        "저희 냥이도 그런 시기가 있었어요. 금방 지나갈 거예요!",
        "글 보다가 눈물날 뻔했어요. 반려동물은 진짜 가족이에요.",
        "저는 유튜브에서 훈련 영상 보고 따라 했더니 효과 있었어요.",
        "이런 정보 공유해주셔서 너무 감사해요. 도움 많이 됐어요!",
        "다들 반려동물 키우면서 행복하시죠? 저는 매일 힐링해요.",
        "저도 비슷한 상황인데 같이 정보 공유해요!",
        "수의사 선생님도 그게 정상이라고 하시던데 안심하세요~",
        "저희 아이 사진도 올려야겠다. 너무 귀엽게 찍히던데 ㅎㅎ",
        "반려동물 커뮤니티 최고예요! 항상 도움받고 가요.",
        "처음 입양했을 때 저도 엄청 걱정했는데 지금은 잘 지내고 있어요.",
        "좋은 내용 공유해주셔서 감사해요. 저장해가요!",
        "우리 동네에도 반려동물 친구들 많았으면 좋겠어요 ㅎㅎ"
    };

    // 맨션 댓글 템플릿 - {mention} 자리에 @[닉네임](user:id) 포맷이 들어감
    private static final String[] MENTION_TEMPLATES = {
        "{mention} 님, 혹시 이 문제 어떻게 해결하셨어요?",
        "{mention} 님도 비슷한 경험 있으시죠? 조언 부탁드려요!",
        "{mention} 님이 저번에 추천해주신 방법 써봤는데 효과 있었어요!",
        "{mention} 님 글 잘 읽었어요. 저도 같은 고민이에요.",
        "아 {mention} 님! 그거 저도 알고 싶었는데 혹시 알려주실 수 있나요?",
        "{mention} 님 혹시 어느 병원 다니세요? 좋은 곳 추천해주시면 감사하겠어요.",
        "{mention} 님 댓글 보고 저도 시도해봤어요. 감사합니다!",
        "{mention} 님 덕분에 고민 해결했어요. 정말 감사해요!",
        "{mention} 님은 이런 경우 어떻게 하셨어요?",
        "{mention} 님도 같은 증상이었나요? 어떻게 나아졌는지 궁금해요."
    };

    private static final String[] DUMMY_IMAGE_KEYS = {
        "POST_IMAGE/dummy_01.png", "POST_IMAGE/dummy_02.png", "POST_IMAGE/dummy_03.png",
        "POST_IMAGE/dummy_04.png", "POST_IMAGE/dummy_05.png", "POST_IMAGE/dummy_06.png",
        "POST_IMAGE/dummy_07.png", "POST_IMAGE/dummy_08.png", "POST_IMAGE/dummy_09.png",
        "POST_IMAGE/dummy_10.png"
    };

    private static final double BASE_LAT = 37.5665;
    private static final double BASE_LNG = 126.9780;

    @Override
    @Transactional
    public void run(String... args) {
        if (termsRepository.count() > 0) {
            log.info("더미 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("더미 데이터 초기화 시작...");

        List<File> dummyImages = createDummyImageFiles();
        List<Terms> termsList = createTerms();
        List<Member> members = createMembersWithAgreementsAndTokens(termsList);
        createPetsWithAllergies(members, dummyImages);
        List<Post> posts = createPostsWithFiles(members, dummyImages);
        createPostLikes(members, posts);
        createComments(members, posts);
        createMentionComments(members, posts);

        log.info("더미 데이터 초기화 완료.");
    }

    private List<File> createDummyImageFiles() {
        List<File> files = new ArrayList<>(DUMMY_IMAGE_KEYS.length);
        for (String key : DUMMY_IMAGE_KEYS) {
            files.add(File.builder()
                .fileName(key.substring(key.lastIndexOf('/') + 1))
                .fileKey(key)
                .fileSize(300_000L)
                .fileType(FileType.IMAGE)
                .build());
        }
        fileRepository.saveAll(files);
        log.info("더미 이미지 파일 {}개 생성 완료", files.size());
        return files;
    }

    private List<Terms> createTerms() {
        List<Terms> list = new ArrayList<>();

        list.add(Terms.builder()
            .title("서비스 이용약관")
            .content("""
                제1조(목적)
                이 약관은 Arfly 서비스 운영 팀(이하 "운영자")가 운영하는 서비스(이하 "서비스")의 이용과 관련하여, 운영자와 회원 사이의 권리·의무 및 책임사항, 서비스 이용절차 등 필요한 사항을 정하는 것을 목적으로 합니다.

                제2조(약관의 공지 및 변경)
                1. 운영자는 관련 법령을 위반하지 않는 범위에서 본 약관을 수정하거나 변경할 수 있습니다.
                2. 운영자가 약관을 변경하는 경우에는 변경 내용과 시행일을 명확히 표시하여, 시행일 전에 서비스 화면 또는 공지사항 등을 통해 안내합니다.
                3. 변경된 약관의 내용이 회원에게 중대한 영향을 미치는 경우, 운영자는 일반 공지 외에 전자우편이나 서비스 내 알림 등 적절한 방법으로 추가 안내할 수 있습니다.
                4. 회원은 변경된 약관에 동의하지 않을 경우 서비스 이용을 중단하고 회원탈퇴를 요청할 수 있습니다.
                5. 운영자가 변경 약관을 공지 또는 통지하면서 일정 기간 내 별도의 거부의사를 표시하지 않고 서비스를 계속 이용하는 경우 변경된 약관에 동의한 것으로 본다는 내용을 함께 안내한 때에는, 회원이 해당 기간 동안 명시적인 거부의사를 표시하지 아니한 경우 변경된 약관에 동의한 것으로 봅니다.
                6. 본 약관에 정하지 아니한 사항은 관련 법령, 개인정보처리방침 및 운영자가 별도로 정한 운영정책에 따릅니다.

                제3조(용어의 정의)
                이 약관에서 사용하는 주요 용어의 뜻은 다음과 같습니다.
                1. 서비스: Arfly 운영팀(이하 "운영자")이 제공하는 반려동물 피부 상태 분석, 프로필 관리, 커뮤니티, 알림, 지도 및 기타 관련 기능 일체
                2. 회원: 본 약관에 동의하고 운영자가 정한 절차에 따라 가입하여 서비스를 이용하는 자
                3. 반려동물 정보: 회원이 서비스 이용을 위해 등록한 반려동물의 이름, 품종, 성별, 나이, 체중, 알레르기 정보, 프로필 사진, 피부 상태 사진 및 기타 회원이 입력하거나 업로드한 정보
                4. 보호자 정보: 회원 식별 및 서비스 제공을 위해 등록한 회원의 계정정보, 연락처, 프로필 정보 등 운영자가 수집하는 정보
                5. 업로드 콘텐츠: 회원이 서비스에 등록하거나 게시하는 사진, 글, 댓글, 파일, 링크, 반려동물 프로필, 피부 상태 이미지 등 일체의 정보
                6. AI 분석 결과: 회원이 업로드한 반려동물 사진 및 입력정보를 바탕으로 서비스가 제공하는 예측, 분류, 요약, 리포트 등 정보성 결과물
                7. 탈퇴: 회원이 서비스 이용계약을 종료하는 의사표시

                제4조(이용계약의 성립)
                1. 서비스 이용계약은 서비스를 이용하려는 자가 약관에 동의하고 회원가입을 신청한 뒤, 운영자가 이를 승낙함으로써 성립합니다.
                2. 운영자는 통상적으로 가입 신청을 승낙하되, 운영상 또는 기술상 필요가 있는 경우 승낙을 유보하거나 제한할 수 있습니다.
                3. 회원가입 시 입력한 정보는 본인의 정확한 정보여야 하며, 타인의 정보를 도용하거나 허위 내용을 기재해서는 안 됩니다.

                제7조(커뮤니티 이용 규칙)
                사용자가 커뮤니티에 등록한 게시물(사진, 글 등)의 저작권은 사용자에게 있습니다. 단, 타인의 명예를 훼손하거나, 불법적인 내용을 포함하거나, 서비스 운영 정책에 위배되는 게시물은 사전 통보 없이 삭제 또는 블라인드 처리될 수 있습니다.
                """)
            .version("1.0")
            .latest(true)
            .required(true)
            .orderIndex(1)
            .build());

        list.add(Terms.builder()
            .title("개인정보 처리방침")
            .content("""
                개인정보 수집 및 이용 동의

                Arf-ly는 회원가입 및 서비스 제공을 위하여 아래와 같이 개인정보를 수집·이용합니다.

                1. 수집 항목
                • 이메일
                • 비밀번호
                • 전화번호
                • 닉네임
                • 반려동물 정보(이름, 품종, 성별, 나이 등)
                • 회원이 업로드한 사진
                • 소셜 로그인 이용 시 제공받는 계정 정보
                • 서비스 이용 과정에서 생성되는 기록 정보

                2. 수집 및 이용 목적
                • 회원가입 및 로그인 처리
                • 본인 확인 및 전화번호 인증
                • 반려동물 정보 등록 및 관리
                • AI 분석 및 건강 리포트 제공
                • 커뮤니티 기능 제공
                • 고객 문의 대응
                • 서비스 운영, 오류 확인 및 품질 개선

                3. 보유 및 이용 기간
                회원 탈퇴 시까지 보유 및 이용합니다.
                다만, 관계 법령에 따라 보관이 필요한 경우 해당 기간 동안 보관할 수 있습니다.

                4. 동의 거부 권리 및 불이익
                이용자는 개인정보 수집 및 이용에 대한 동의를 거부할 권리가 있습니다.
                다만, 필수 항목에 대한 동의를 거부할 경우 회원가입 및 서비스 이용이 제한될 수 있습니다.
                """)
            .version("1.0")
            .latest(true)
            .required(true)
            .orderIndex(2)
            .build());

        list.add(Terms.builder()
            .title("AI 진단 결과는 의료행위가 아님에 동의")
            .content("""
                Arfly에서 제공하는 AI 분석 결과는 반려동물의 피부 상태에 대한 참고용 정보일 뿐,
                수의학적 진단이나 치료를 대체하지 않습니다.

                이용자는 서비스에서 제공하는 결과를 기반으로 한 모든 판단과 행동에 대해
                전적으로 본인의 책임 하에 결정하여야 합니다.

                회사는 AI 분석 결과의 정확성, 신뢰성, 완전성을 보장하지 않으며,
                해당 결과를 이용함으로써 발생하는 어떠한 손해에 대해서도 책임을 지지 않습니다.
                """)
            .version("1.0")
            .latest(true)
            .required(true)
            .orderIndex(3)
            .build());

        list.add(Terms.builder()
            .title("푸시 알림 수신 동의")
            .content("""
                회원은 Arfly 서비스 이용과 관련하여 다음과 같은 안내를 푸시 알림, 앱 내 알림, 문자 또는 이메일 등의 방법으로 받을 수 있습니다.

                1. 계정 및 회원가입 관련 안내
                • 회원가입 완료, 로그인 보안 안내, 비밀번호 재설정, 계정 보호 관련 알림

                2. 일정 및 맞춤 알림 관련 안내
                • 회원이 직접 설정한 복약 알림, 케어 일정, 반복 알림, 리마인드 알림
                """)
            .version("1.0")
            .latest(true)
            .required(false)
            .orderIndex(4)
            .build());

        list.add(Terms.builder()
            .title("야간 알림 수신 동의")
            .content("""
                관련 법령에 따라 야간 시간대(21:00 ~ 익일 08:00)에 광고성 알림을 전송하기 위해서는 별도의 동의가 필요합니다.
                주의: 반려동물의 생체 신호 이상 등 긴급 건강 알림은 야간 동의 여부와 관계없이 전송될 수 있습니다.
                """)
            .version("1.0")
            .latest(true)
            .required(false)
            .orderIndex(5)
            .build());

        list.add(Terms.builder()
            .title("AI 기능 개선을 위한 데이터 활용 동의")
            .content("""
                운영자는 회원이 서비스에 업로드하거나 입력한 반려동물 사진 및 관련 정보를 활용하여 서비스 품질 개선 및 AI 기능 향상을 위한 연구·개선 작업을 수행할 수 있습니다.

                1. 활용 대상 정보
                • 반려동물 피부 사진
                • 반려동물 프로필 정보(예: 품종, 나이, 성별, 체중, 알레르기 정보 등)
                • 분석 결과 및 이용 과정에서 생성된 오류·성능 개선용 정보

                2. 활용 목적
                • AI 분석 정확도 및 품질 향상
                • 오진 가능성 감소 및 결과 표현 개선
                • 서비스 기능 개선 및 사용자 경험 향상
                • 모델 성능 검증, 테스트 및 운영 안정화

                3. 철회
                • 회원은 언제든지 해당 동의를 철회할 수 있습니다.
                """)
            .version("1.0")
            .latest(true)
            .required(false)
            .orderIndex(6)
            .build());

        list.add(Terms.builder()
            .title("마케팅 수신 동의")
            .content("Arfly 서비스의 마케팅 정보 및 이벤트 안내를 푸시 알림, 이메일 등의 방법으로 수신하는 것에 동의합니다.")
            .version("1.0")
            .latest(true)
            .required(false)
            .orderIndex(7)
            .build());

        termsRepository.saveAll(list);
        log.info("약관 {}개 생성 완료", list.size());
        return list;
    }

    private List<Member> createMembersWithAgreementsAndTokens(List<Terms> termsList) {
        String encodedPassword = passwordEncoder.encode("Test1234!");
        List<Member> members = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            double latOffset = (random.nextDouble() - 0.5) * 0.1;
            double lngOffset = (random.nextDouble() - 0.5) * 0.1;

            members.add(Member.builder()
                .userId("dummy_user_" + String.format("%03d", i + 1))
                .password(encodedPassword)
                .nickName(MEMBER_NICKNAMES[i])
                .phoneNumber("010" + String.format("%04d", 1000 + i) + String.format("%04d", 2000 + i))
                .role(Role.USER)
                .latitude(BASE_LAT + latOffset)
                .longitude(BASE_LNG + lngOffset)
                .notificationEnabled(true)
                .build());
        }
        memberRepository.saveAll(members);

        // 약관 동의 - 필수는 true, 선택은 랜덤
        List<UserTermsAgreement> agreements = new ArrayList<>();
        for (Member member : members) {
            for (Terms terms : termsList) {
                agreements.add(UserTermsAgreement.builder()
                    .member(member)
                    .terms(terms)
                    .agreement(terms.getRequired() || random.nextBoolean())
                    .build());
            }
        }
        userTermsAgreementRepository.saveAll(agreements);

        // FCM 토큰 - 멤버마다 1개, 가짜 토큰
        DeviceType[] deviceTypes = DeviceType.values();
        List<FcmToken> fcmTokens = new ArrayList<>();
        for (Member member : members) {
            fcmTokens.add(FcmToken.builder()
                .member(member)
                .token("fake-fcm-" + UUID.randomUUID())
                .deviceType(deviceTypes[random.nextInt(deviceTypes.length)])
                .build());
        }
        fcmTokenRepository.saveAll(fcmTokens);

        log.info("멤버 {}명, 약관 동의 {}개, FCM 토큰 {}개 생성 완료",
            members.size(), agreements.size(), fcmTokens.size());
        return members;
    }

    private void createPetsWithAllergies(List<Member> members, List<File> dummyImages) {
        List<Breeds> dogBreeds = breedsRepository.findBySpecies(Species.DOG);
        List<Breeds> catBreeds = breedsRepository.findBySpecies(Species.CAT);

        if (dogBreeds.isEmpty() || catBreeds.isEmpty()) {
            log.warn("품종 데이터 없음 - 펫 생성 건너뜀");
            return;
        }

        Sex[] sexValues = Sex.values();
        List<Pet> pets = new ArrayList<>();

        int petIndex = 0;
        for (Member member : members) {
            for (int i = 0; i < 2; i++) {
                Species species = random.nextBoolean() ? Species.DOG : Species.CAT;
                List<Breeds> breedPool = species == Species.DOG ? dogBreeds : catBreeds;
                Breeds breed = breedPool.get(random.nextInt(breedPool.size()));

                pets.add(Pet.builder()
                    .member(member)
                    .breeds(breed)
                    .profileImage(petIndex < 5 ? dummyImages.get(petIndex) : null)
                    .name(PET_NAMES[random.nextInt(PET_NAMES.length)])
                    .species(species)
                    .sex(sexValues[random.nextInt(sexValues.length)])
                    .neutered(random.nextBoolean())
                    .birth(2016 + random.nextInt(9))
                    .weight(Math.round((1.0 + random.nextDouble() * 29.0) * 10.0) / 10.0)
                    .build());
                petIndex++;
            }
        }
        petRepository.saveAll(pets);

        // 알레르기 1~2개씩
        List<PetAllergy> allergies = new ArrayList<>();
        for (Pet pet : pets) {
            int count = 1 + random.nextInt(2);
            Set<String> chosen = new HashSet<>();
            while (chosen.size() < count) {
                chosen.add(ALLERGIES[random.nextInt(ALLERGIES.length)]);
            }
            for (String allergyName : chosen) {
                allergies.add(PetAllergy.builder()
                    .pet(pet)
                    .name(allergyName)
                    .build());
            }
        }
        petAllergyRepository.saveAll(allergies);

        log.info("펫 {}마리, 알레르기 {}개 생성 완료", pets.size(), allergies.size());
    }

    private List<Post> createPostsWithFiles(List<Member> members, List<File> dummyImages) {
        // 게시글 100개
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Member member = members.get(random.nextInt(members.size()));
            posts.add(Post.builder()
                .member(member)
                .title(POST_TITLES[i % POST_TITLES.length])
                .content(POST_CONTENTS[random.nextInt(POST_CONTENTS.length)])
                .build());
        }
        postRepository.saveAll(posts);

        // 가장 나중에 생성된 게시물(정렬 최상단)에만 이미지 5개 배치
        Post topPost = posts.get(posts.size() - 1);
        List<PostImage> postImages = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            postImages.add(PostImage.builder()
                .post(topPost)
                .file(dummyImages.get(5 + i))
                .orderIndex(i + 1)
                .build());
        }
        postImageRepository.saveAll(postImages);

        log.info("게시글 {}개, 게시물 이미지 {}개 생성 완료", posts.size(), postImages.size());
        return posts;
    }

    private void createPostLikes(List<Member> members, List<Post> posts) {
        // 300개 무작위, 동일 member+post 중복 불가
        Set<String> usedPairs = new HashSet<>();
        List<PostLike> likes = new ArrayList<>(300);
        Map<Long, Integer> likeCountMap = new HashMap<>();
        Map<Long, Set<String>> likeUsersMap = new HashMap<>();

        int maxAttempts = 10_000;
        int attempts = 0;
        while (likes.size() < 300 && attempts < maxAttempts) {
            attempts++;
            Member member = members.get(random.nextInt(members.size()));
            Post post = posts.get(random.nextInt(posts.size()));
            String pair = member.getId() + "-" + post.getId();

            if (usedPairs.add(pair)) {
                likes.add(PostLike.builder()
                    .member(member)
                    .post(post)
                    .build());
                likeCountMap.merge(post.getId(), 1, Integer::sum);
                likeUsersMap.computeIfAbsent(post.getId(), k -> new HashSet<>())
                    .add(String.valueOf(member.getId()));
            }
        }
        postLikeRepository.saveAll(likes);

        // DB likeCount 동기화
        entityManager.flush();
        likeCountMap.forEach((postId, count) ->
            entityManager.createQuery("UPDATE Post p SET p.likeCount = :count WHERE p.id = :id")
                .setParameter("count", count)
                .setParameter("id", postId)
                .executeUpdate()
        );

        // Redis likeCount 및 유저 Set 동기화
        likeCountMap.forEach((postId, count) ->
            redisTemplate.opsForValue().set("post:like:" + postId, String.valueOf(count))
        );
        likeUsersMap.forEach((postId, userIds) ->
            redisTemplate.opsForSet().add("post:like:users:" + postId, userIds.toArray(new String[0]))
        );

        log.info("게시글 좋아요 {}개 생성 완료", likes.size());
    }

    private void createComments(List<Member> members, List<Post> posts) {
        List<Comment> comments = new ArrayList<>();
        for (Post post : posts) {
            int count = 3 + random.nextInt(3); // 3~5개
            for (int i = 0; i < count; i++) {
                Member member = members.get(random.nextInt(members.size()));
                comments.add(Comment.builder()
                    .post(post)
                    .member(member)
                    .content(COMMENT_CONTENTS[random.nextInt(COMMENT_CONTENTS.length)])
                    .build());
            }
        }
        commentRepository.saveAll(comments);
        log.info("댓글 {}개 생성 완료", comments.size());
    }

    private void createMentionComments(List<Member> members, List<Post> posts) {
        List<Comment> comments = new ArrayList<>();
        List<CommentMention> mentions = new ArrayList<>();

        for (Post post : posts) {
            // 게시물당 1개의 맨션 댓글 생성 (총 100개)
            Member commenter = members.get(random.nextInt(members.size()));
            // 자기 자신은 맨션하지 않도록 다른 멤버 선택
            Member mentioned;
            do {
                mentioned = members.get(random.nextInt(members.size()));
            } while (mentioned.getId().equals(commenter.getId()));

            String template = MENTION_TEMPLATES[random.nextInt(MENTION_TEMPLATES.length)];
            String mentionTag = "@[" + mentioned.getNickName() + "](user:" + mentioned.getId() + ")";
            String content = template.replace("{mention}", mentionTag);

            Comment comment = Comment.builder()
                .post(post)
                .member(commenter)
                .content(content)
                .build();
            comments.add(comment);
        }
        commentRepository.saveAll(comments);

        for (int i = 0; i < comments.size(); i++) {
            Comment comment = comments.get(i);
            // content에서 user:id 파싱하여 mentionedUser 특정
            String content = comment.getContent();
            int userIdStart = content.indexOf("(user:") + 6;
            int userIdEnd = content.indexOf(")", userIdStart);
            Long mentionedId = Long.parseLong(content.substring(userIdStart, userIdEnd));

            members.stream()
                .filter(m -> m.getId().equals(mentionedId))
                .findFirst()
                .ifPresent(mentionedUser ->
                    mentions.add(CommentMention.builder()
                        .comment(comment)
                        .mentionedUser(mentionedUser)
                        .build())
                );
        }
        commentMentionRepository.saveAll(mentions);

        log.info("맨션 댓글 {}개, CommentMention {}개 생성 완료", comments.size(), mentions.size());
    }

}
