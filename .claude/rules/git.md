# Git 규칙

## 커밋 메시지
- 형식: `<type>: <제목>` (예: `feat: 유저 조회 API 추가`)
- type: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `style`, `perf`
- 제목은 50자 이내, 명령형 어미 지양(한글: "추가", "수정" 등 명사형).

## 브랜치
- `main`: 배포 가능한 상태 유지. 직접 push 금지 (로컬 pre-push 훅 + GitHub 브랜치 보호로 이중 차단됨).
- 기능: `feature/<이슈번호>-<간단설명>`
- 버그: `fix/<이슈번호>-<간단설명>`

## 워크플로우

### 최초 clone 시 (1회)
로컬 pre-push 훅을 이 저장소의 훅 경로로 등록한다.
```bash
git config core.hooksPath .githooks
```

### 기능 개발 사이클
```bash
git switch main && git pull
git switch -c feature/<이슈번호>-<간단설명>
# ... 코드 작성 → 커밋
git push -u origin feature/<이슈번호>-<간단설명>
gh pr create
gh pr merge --squash --delete-branch
git switch main && git pull
```

상세 치트시트는 `docs/git-workflow.md` 참고.

## 커밋 대상 제외
- IDE 메타데이터(`.idea/`), 빌드 산출물(`target/`), 로컬 설정, 비밀값은 반드시 `.gitignore`.
- `.claude/settings.local.json`은 개인 설정 → 커밋 금지.

## 금지
- `git push --force` (개인 브랜치가 아닌 공유 브랜치 대상)
- `--no-verify`로 훅 우회
- 자격증명/비밀값이 포함된 커밋 (실수 시 즉시 rotate 후 히스토리 정리)
